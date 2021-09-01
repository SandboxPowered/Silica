package org.sandboxpowered.silica.world

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.artemis.WorldConfigurationBuilder
import com.mojang.authlib.GameProfile
import org.sandboxpowered.silica.SilicaPlayerManager
import org.sandboxpowered.silica.component.VanillaPlayerInput
import org.sandboxpowered.silica.network.getSystem
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.state.fluid.FluidState
import org.sandboxpowered.silica.system.Entity3dMap
import org.sandboxpowered.silica.system.Entity3dMapSystem
import org.sandboxpowered.silica.system.VanillaInputSystem
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.Side
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.util.extensions.registerAs
import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.world.gen.TerrainGenerator
import org.sandboxpowered.silica.world.util.BlocTree
import org.sandboxpowered.silica.world.util.OcTree
import org.sandboxpowered.silica.world.util.iterateCube
import java.util.*
import com.artemis.World as ArtemisWorld
import org.sandboxpowered.silica.world.gen.TerrainGenerator.Generate as CommandGenerate

class SilicaWorld private constructor(private val side: Side) : World {

    private val blocks: BlocTree = BlocTree(
        WORLD_MIN,
        WORLD_MIN,
        WORLD_MIN,
        WORLD_SIZE,
        SilicaRegistries.BLOCK_REGISTRY[Identifier.of("air")].get().defaultState
    )
    val artemisWorld: ArtemisWorld
    private var worldTicks = 0L

    init {
        val config = WorldConfigurationBuilder()
        config.with(VanillaInputSystem())
        config.with(SilicaPlayerManager(10))
        val entityMap = Entity3dMapSystem(
            OcTree(
                WORLD_MIN.toFloat(), WORLD_MIN.toFloat(), WORLD_MIN.toFloat(),
                WORLD_SIZE.toFloat(), WORLD_SIZE.toFloat(), WORLD_SIZE.toFloat()
            )
        )
        config.with(entityMap)
        artemisWorld = ArtemisWorld(
            config.build()
                .registerAs<Entity3dMap>(entityMap)
        )
    }

    override fun getBlockState(pos: Position): BlockState {
        return blocks[pos.x, pos.y, pos.z]
    }

    override fun setBlockState(pos: Position, state: BlockState) {
        blocks[pos.x, pos.y, pos.z] = state
    }

    override fun getFluidState(pos: Position): FluidState {
        TODO("Not yet implemented")
    }

    fun getTerrain(): BlocTree = this.blocks

    companion object {
        private const val WORLD_MIN = -1 shl 25 // -2^25
        private const val WORLD_MAX = (1 shl 25) - 1 // (2^25)-1
        private const val WORLD_SIZE = -WORLD_MIN + WORLD_MAX + 1 // 2^26

        fun actor(side: Side): Behavior<Command> = Behaviors.setup {
            Actor(SilicaWorld(side), it)
        }
    }

    sealed class Command {
        class Tick(val delta: Float, val replyTo: ActorRef<Tock>) : Command() {
            class Tock(val done: ActorRef<Command>)
        }

        /**
         * Only to be used for reading data !
         */
        class Ask<T>(val body: (WorldReader) -> T, val replyTo: ActorRef<T>) : Command()

        /**
         * Will be processed during next tick
         */
        sealed class DelayedCommand<F : World, R : Any>(val body: (F) -> R) : Command() {
            class Perform(body: (World) -> Unit) : DelayedCommand<World, Unit>(body)

            /**
             * To be avoided when possible
             */
            class PerformSilica(body: (SilicaWorld) -> Unit) : DelayedCommand<SilicaWorld, Unit>(body)

            /**
             * To be avoided when possible
             */
            class AskSilica<T : Any>(body: (SilicaWorld) -> T, val replyTo: ActorRef<T>) :
                DelayedCommand<SilicaWorld, T>(body)

            companion object {
                inline fun <T : Any> createPlayer(
                    gameProfile: GameProfile,
                    replyTo: ActorRef<T>,
                    crossinline transform: (VanillaPlayerInput, Array<GameProfile>) -> T
                ) = AskSilica(
                    {
                        val playerManager = it.artemisWorld.getSystem<SilicaPlayerManager>()
                        transform(playerManager.create(gameProfile), playerManager.getOnlinePlayerProfiles())
                    },
                    replyTo
                )
            }
        }
    }

    private class Actor(private val world: SilicaWorld, context: ActorContext<Command>) :
        AbstractBehavior<Command>(context) {

        private val commandQueue: Deque<Command.DelayedCommand<*, *>> = LinkedList()
        private val generator: ActorRef<TerrainGenerator> =
            context.spawn(TerrainGenerator.actor(), "terrain_generator")
        private var generated = false

        override fun createReceive(): Receive<Command> = newReceiveBuilder()
            .onMessage(this::handleTick)
            .onMessage(this::handleAsk)
            .onMessage(this::handleDelayedCommand)
            .build()

        private fun handleTick(tick: Command.Tick): Behavior<Command> {
            if (!generated) {
                enqueueGeneration(generator)
                generated = true
            }
            this.processCommandQueue()

            val w = world.artemisWorld
            w.delta = tick.delta
            w.process()
            ++world.worldTicks

            tick.replyTo.tell(Command.Tick.Tock(context.self))
            return Behaviors.same()
        }

        private fun processCommandQueue() {
            var next = commandQueue.pollFirst()
            while (next != null) {
                when (next) {
                    is Command.DelayedCommand.Perform -> next.body(world)
                    is Command.DelayedCommand.PerformSilica -> next.body(world)
                    is Command.DelayedCommand.AskSilica<*> -> {
                        @Suppress("UNCHECKED_CAST")
                        val replyTo = next.replyTo as ActorRef<Any>
                        replyTo.tell(next.body(world))
                    }
                    else -> error("Unhandled command in queue : $next")
                }
                next = commandQueue.pollFirst()
            }
        }

        private fun handleDelayedCommand(command: Command.DelayedCommand<*, *>): Behavior<Command> {
            commandQueue += command
            return Behaviors.same()
        }

        private fun handleAsk(ask: Command.Ask<Any>): Behavior<Command> {
            ask.replyTo.tell(ask.body(world))
            return Behaviors.same()
        }

        // TODO: TMP !!
        private fun enqueueGeneration(to: ActorRef<in CommandGenerate>) {
            iterateCube(-5, 0, -5, w = 10, h = 1) { dx, dy, dz ->
                val x = dx * 16
                val z = dz * 16
                to.tell(CommandGenerate(x, dy, z, world.blocks[x, dy, z, 16, 16, 16], context.system.ignoreRef()))
            }
        }
    }


}