package org.sandboxpowered.silica.world

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.api.block.Blocks
import org.sandboxpowered.api.ecs.CapabilityManager
import org.sandboxpowered.api.ecs.ComponentMapper
import org.sandboxpowered.api.ecs.EntityBlueprint
import org.sandboxpowered.api.ecs.component.Component
import org.sandboxpowered.api.item.ItemStack
import org.sandboxpowered.api.state.BlockState
import org.sandboxpowered.api.state.FluidState
import org.sandboxpowered.api.tags.TagManager
import org.sandboxpowered.api.util.Side
import org.sandboxpowered.api.util.math.Position
import org.sandboxpowered.api.world.BlockFlag
import org.sandboxpowered.api.world.World
import org.sandboxpowered.api.world.WorldReader
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.world.gen.TerrainGenerator
import org.sandboxpowered.silica.world.util.BlocTree
import org.sandboxpowered.silica.world.util.iterateCube
import java.util.*
import org.sandboxpowered.silica.world.gen.TerrainGenerator.Command.Generate as CommandGenerate

class SilicaWorld private constructor(private val side: Side) : World {

    private val blocks: BlocTree = BlocTree(WORLD_MIN, WORLD_MIN, WORLD_MIN, WORLD_SIZE, Blocks.AIR.get().baseState)
    private val artemisWorld: com.artemis.World? = null
    private var worldTicks = 0L

    override fun getBlockState(position: Position): BlockState {
        return blocks[position.x, position.y, position.z]
    }

    override fun getBlockEntity(position: Position): Int {
        TODO("Not yet implemented")
    }

    override fun setBlockState(position: Position, state: BlockState, vararg flags: BlockFlag): Boolean {
        blocks[position.x, position.y, position.z] = state
        return true
    }

    override fun getFluidState(position: Position): FluidState {
        return getBlockState(position).fluidState
    }


    override fun getWorldTime(): Long {
        return worldTicks
    }

    override fun getTagManager(): TagManager? {
        TODO("Not yet implemented")
    }

    override fun <T : Component?> getMapper(type: Class<T>): ComponentMapper<T> {
        TODO("Not yet implemented")
    }

    override fun createEntity(): Int {
        TODO("Not yet implemented")
    }

    override fun createEntity(blueprint: EntityBlueprint): Int {
        TODO("Not yet implemented")
    }

    override fun removeEntity(entity: Int) {
        TODO("Not yet implemented")
    }

    override fun getCapabilityManager(): CapabilityManager {
        TODO("Not yet implemented")
    }

    override fun getSide(): Side = this.side

    override fun spawnItem(x: Double, y: Double, z: Double, stack: ItemStack) {
        TODO("Not yet implemented")
    }

    // TODO: Tmp. Should be read-only, and network should be actorized
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

        class Perform(val body: (World) -> Unit) : Command()
        class Ask<T>(val body: (WorldReader) -> T, val replyTo: ActorRef<T>) : Command()
    }

    private class Actor(private val world: SilicaWorld, context: ActorContext<Command>) :
        AbstractBehavior<Command>(context) {

        private val commandQueue: Deque<Command> = LinkedList()
        private val generator: ActorRef<TerrainGenerator.Command> =
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
            if (w != null) {
                w.delta = tick.delta
                w.process()
            }
            ++world.worldTicks

            tick.replyTo.tell(Command.Tick.Tock(context.self))
            return Behaviors.same()
        }

        private fun processCommandQueue() {
            var next = commandQueue.pollFirst()
            while (next != null) {
                when (next) {
                    is Command.Perform -> next.body(world)
                    else -> error("Unhandled command in queue : $next")
                }
                next = commandQueue.pollFirst()
            }
        }

        private fun handleDelayedCommand(command: Command): Behavior<Command> {
            commandQueue += command
            return Behaviors.same()
        }

        private fun handleAsk(ask: Command.Ask<Any>): Behavior<Command> {
            ask.replyTo.tell(ask.body(world))
            return Behaviors.same()
        }

        // TODO: TMP !!
        private fun enqueueGeneration(to: ActorRef<in CommandGenerate>) {
            iterateCube(-3, 0, -3, w = 6, h = 1) { dx, dy, dz ->
                val x = dx * 16
                val z = dz * 16
                to.tell(CommandGenerate(x, dy, z, world.blocks[x, dy, z, 16, 16, 16], context.system.ignoreRef()))
            }
        }
    }


}