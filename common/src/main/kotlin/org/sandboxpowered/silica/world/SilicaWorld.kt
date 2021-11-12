package org.sandboxpowered.silica.world

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.artemis.Archetype
import com.artemis.EntityEdit
import com.artemis.WorldConfigurationBuilder
import com.artemis.utils.ImmutableIntBag
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.mostlyoriginal.api.event.common.EventSystem
import org.joml.Vector2i
import org.joml.Vector2ic
import org.sandboxpowered.silica.SilicaInternalAPI
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.block.BlockEntityProvider
import org.sandboxpowered.silica.api.ecs.component.BlockPositionComponent
import org.sandboxpowered.silica.api.ecs.component.PlayerInventoryComponent
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.entity.EntityEvents
import org.sandboxpowered.silica.api.internal.InternalAPI
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.Side
import org.sandboxpowered.silica.api.util.extensions.add
import org.sandboxpowered.silica.api.util.extensions.create
import org.sandboxpowered.silica.api.util.extensions.getSystem
import org.sandboxpowered.silica.api.util.extensions.registerAs
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.api.world.WorldReader
import org.sandboxpowered.silica.api.world.WorldWriter
import org.sandboxpowered.silica.api.world.generation.WorldGenerator
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.api.world.state.fluid.FluidState
import org.sandboxpowered.silica.ecs.component.EntityIdentity
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput
import org.sandboxpowered.silica.ecs.events.InitializeArchetypeEvent
import org.sandboxpowered.silica.ecs.events.ReplaceBlockEvent
import org.sandboxpowered.silica.ecs.events.SpawnEntityEvent
import org.sandboxpowered.silica.ecs.system.*
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.world.gen.TerrainGenerator
import org.sandboxpowered.silica.world.util.BlocTree
import org.sandboxpowered.silica.world.util.IntTree
import org.sandboxpowered.silica.world.util.OcTree
import org.sandboxpowered.silica.world.util.iterateCube
import java.util.*
import com.artemis.World as ArtemisWorld
import org.sandboxpowered.silica.world.gen.TerrainGenerator.Generate as CommandGenerate

class SilicaWorld private constructor(val side: Side, val server: SilicaServer) : World {
    private val blocks: BlocTree = BlocTree(
        WORLD_MIN,
        WORLD_MIN,
        WORLD_MIN,
        WORLD_SIZE,
        Registries.BLOCKS[Identifier("air")].get().defaultState
    )
    val artemisWorld: ArtemisWorld
    private var worldTicks = 0L

    private val eventSystem = EventSystem()

    override val worldHeight: Vector2ic = Vector2i(worldGenerator.minWorldHeight, worldGenerator.maxWorldHeight)
    override val isClient: Boolean = side == Side.CLIENT
    override val isServer: Boolean = side == Side.SERVER

    init {
        val config = WorldConfigurationBuilder()
        config.with(eventSystem)
        config.with(VanillaInputSystem(server))
        config.with(SilicaPlayerManager(10))
        val entityMap = Entity3dMapSystem(
            OcTree(
                WORLD_MIN.toFloat(), WORLD_MIN.toFloat(), WORLD_MIN.toFloat(),
                WORLD_SIZE.toFloat(), WORLD_SIZE.toFloat(), WORLD_SIZE.toFloat()
            ),
            IntTree(
                WORLD_MIN, WORLD_MIN, WORLD_MIN,
                WORLD_SIZE, WORLD_SIZE, WORLD_SIZE
            )
        )
        SilicaRegistries.BLOCKS_WITH_ENTITY.forEach {
            it.createProcessingSystem()?.let { system -> config.with(it.processingSystemPriority, system) }
        }
        SilicaRegistries.SYSTEM_REGISTRY.forEach {
            config.with(0 /* TODO: insert actual prio */, it)
        }
        config.with(Int.MAX_VALUE /* first */, entityMap)
        config.with(Int.MIN_VALUE /* last */, EntityRemovalSystem())
        artemisWorld = ArtemisWorld(
            config.build()
                .registerAs<Entity3dMap>(entityMap)
                .registerAs<World>(this)
        )
        artemisWorld.create()
    }

    override fun registerEventSubscriber(sub: Any) = eventSystem.registerEvents(sub)

    override fun getBlockState(pos: Position): BlockState {
        return blocks[pos.x, pos.y, pos.z]
    }

    private val blockArchetypesCache: Object2ObjectMap<Block, Archetype> = Object2ObjectOpenHashMap()
    private val entitiesArchetypesCache: Object2ObjectMap<EntityDefinition, Archetype> = Object2ObjectOpenHashMap()

    override fun setBlockState(pos: Position, state: BlockState, vararg flags: WorldWriter.Flag): Boolean {
        if (isOutOfHeightLimit(pos)) return false
        //TODO see if theres generally any better way of doing this.
        val system = artemisWorld.getSystem<Entity3dMapSystem>()
        val existingBEs = system.getBlockEntities(pos)
        if (!existingBEs.isEmpty) {
            existingBEs.forEach {
                artemisWorld.delete(it)
            }
        }
        val block = state.block
        if (block is BlockEntityProvider) {
            artemisWorld.create(blockArchetypesCache.computeIfAbsent(block) {
                val builder = block.createArchetype()
                builder.add<BlockPositionComponent>()
                builder.build(artemisWorld, "block:${block.identifier}")
            })
        }
        val oldState = blocks[pos.x, pos.y, pos.z]
        blocks[pos.x, pos.y, pos.z] = state

        if (WorldWriter.Flag.NOTIFY_NEIGHBORS in flags) {
            Direction.ALL.forEach { updateNeighbor(pos, state, it.opposite, pos.shift(it)) }
        }

        if (WorldWriter.Flag.NOTIFY_LISTENERS in flags) {
            eventSystem.dispatch(ReplaceBlockEvent(pos, oldState, state))
        }
        return true
    }

    private fun spawnEntity(entityDefinition: EntityDefinition, initialize: (EntityEdit) -> Unit) {
        val id = artemisWorld.create(entitiesArchetypesCache.computeIfAbsent(entityDefinition) {
            val archetype = it.createArchetype()
            EntityEvents.INITIALIZE_ARCHETYPE_EVENT.invoker?.invoke(it, archetype)
            eventSystem.dispatch(InitializeArchetypeEvent(it, archetype))
            archetype.add<EntityIdentity>()
                .build(artemisWorld, "entity:${entityDefinition.identifier}")
        })

        artemisWorld.edit(id).also {
            val identity = it.create<EntityIdentity>()
            identity.uuid = UUID.randomUUID()
            identity.entityDefinition = entityDefinition
            initialize(it)
            eventSystem.dispatch(SpawnEntityEvent(it.entity))
        }
    }

    private fun updateNeighbor(pos: Position, state: BlockState, opposite: Direction, neighbor: Position) {
        val neighborState = getBlockState(neighbor)

        neighborState.block.onNeighborUpdate(this, neighbor, neighborState, pos, state, opposite)
    }

    override fun getFluidState(pos: Position): FluidState {
        TODO("Not yet implemented")
    }

    fun getTerrain(): BlocTree = this.blocks

    companion object {
        lateinit var worldGenerator: WorldGenerator
        private val WORLD_MIN
            get() = worldGenerator.minWorldWidth
        private val WORLD_MAX
            get() = worldGenerator.maxWorldWidth
        private val WORLD_SIZE
            get() = -WORLD_MIN + WORLD_MAX + 1

        fun actor(side: Side, server: SilicaServer): Behavior<Command> = Behaviors.setup {
            Actor(SilicaWorld(side, server), it)
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

        class RegisterEventSubscriber(val sub: Any) : Command()

        /**
         * Will be processed during next tick
         */
        sealed class DelayedCommand<F : World, R : Any>(val body: (F) -> R) : Command() {
            class Perform(body: (World) -> Unit) : DelayedCommand<World, Unit>(body)

            /**
             * To be avoided when possible
             */
            open class PerformSilica(body: (SilicaWorld) -> Unit) : DelayedCommand<SilicaWorld, Unit>(body)

            /**
             * To be avoided when possible
             */
            class AskSilica<T : Any>(val replyTo: ActorRef<T>, body: (SilicaWorld) -> T) :
                DelayedCommand<SilicaWorld, T>(body)

            companion object {
                inline fun <T : Any> createPlayer(
                    gameProfile: GameProfile,
                    replyTo: ActorRef<T>,
                    crossinline transform: (VanillaPlayerInput, PlayerInventoryComponent, Array<GameProfile>) -> T
                ) = AskSilica(
                    replyTo
                ) {
                    val playerManager = it.artemisWorld.getSystem<SilicaPlayerManager>()
                    transform(
                        playerManager.create(gameProfile),
                        playerManager.createInventory(gameProfile),
                        playerManager.getOnlinePlayerProfiles()
                    )
                }

                fun spawnEntity(
                    entityDefinition: EntityDefinition, initialize: (EntityEdit) -> Unit = { }
                ) = PerformSilica {
                    it.spawnEntity(entityDefinition, initialize)
                }
            }
        }
    }

    private class Actor(private val world: SilicaWorld, context: ActorContext<Command>) :
        AbstractBehavior<Command>(context) {

        private val commandQueue: Deque<Command.DelayedCommand<*, *>> = LinkedList()
        private val generator: ActorRef<TerrainGenerator> =
            context.spawn(TerrainGenerator.actor(), "terrain_generator")
        private var generated = false

        init {
            (InternalAPI.instance as SilicaInternalAPI).registerListenerDelegate = {
                context.self.tell(Command.RegisterEventSubscriber(it))
            }
        }

        override fun createReceive(): Receive<Command> = newReceiveBuilder()
            .onMessage(this::handleTick)
            .onMessage(this::handleAsk)
            .onMessage(this::handleDelayedCommand)
            .onMessage(this::handleSubscribe)
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
                try {
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
                } catch (e: Throwable) {
                    context.log.error("Couldn't process command ${next.javaClass.simpleName}", e)
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

        private fun handleSubscribe(message: Command.RegisterEventSubscriber): Behavior<Command> {
            world.registerEventSubscriber(message.sub)
            return Behaviors.same()
        }

        // TODO: TMP !!
        private fun enqueueGeneration(to: ActorRef<in CommandGenerate>) {
            iterateCube(-10, 0, -10, w = 20, h = 1) { dx, dy, dz ->
                val x = dx * 16
                val z = dz * 16
                to.tell(CommandGenerate(x, dy, z, world.blocks[x, dy, z, 16, 16, 16], context.system.ignoreRef()))
            }
        }
    }


}

private fun ImmutableIntBag<Any>.forEach(block: (Int) -> Unit) {
    for (i in 0 until this.size()) {
        block(this.get(i))
    }
}
