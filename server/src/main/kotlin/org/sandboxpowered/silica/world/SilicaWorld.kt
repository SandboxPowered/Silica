package org.sandboxpowered.silica.world

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.artemis.*
import com.artemis.WorldConfigurationBuilder.Priority
import com.artemis.utils.ImmutableIntBag
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.joml.Vector2i
import org.joml.Vector2ic
import org.joml.Vector3f
import org.sandboxpowered.silica.SilicaInternalAPI
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.block.BlockEntityProvider
import org.sandboxpowered.silica.api.block.BlockEvents
import org.sandboxpowered.silica.api.ecs.component.BlockPositionComponent
import org.sandboxpowered.silica.api.ecs.component.EntityIdentity
import org.sandboxpowered.silica.api.ecs.component.MarkForRemovalComponent
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.entity.EntityEvents
import org.sandboxpowered.silica.api.internal.InternalAPI
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.server.PlayerManager
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.Side
import org.sandboxpowered.silica.api.util.extensions.*
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.util.math.toVec3f
import org.sandboxpowered.silica.api.world.*
import org.sandboxpowered.silica.api.world.generation.WorldGenerator
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.api.world.state.fluid.FluidState
import org.sandboxpowered.silica.ecs.system.*
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.world.gen.TerrainGenerator
import org.sandboxpowered.silica.world.persistence.WorldStorage
import org.sandboxpowered.silica.world.util.Bounds
import org.sandboxpowered.silica.world.util.IntTree
import org.sandboxpowered.silica.world.util.OcTree
import org.sandboxpowered.silica.world.util.WorldData
import java.util.*
import java.util.function.Function
import com.artemis.World as ArtemisWorld

class SilicaWorld private constructor(val side: Side, val server: SilicaServer) : World {
    private lateinit var data: WorldData
    val artemisWorld: ArtemisWorld
    private var worldTicks = 0L

    override val playerManager: PlayerManager
        get() = artemisWorld.getSystem<SilicaPlayerManager>()

    override val worldHeight: Vector2ic = Vector2i(worldGenerator.minWorldHeight, worldGenerator.maxWorldHeight)
    override val isClient: Boolean = side == Side.CLIENT
    override val isServer: Boolean = side == Side.SERVER

    private val entityRemovalMapper: BaseComponentMapper<MarkForRemovalComponent>

    init {
        val config = WorldConfigurationBuilder()
        config.with(SilicaPlayerManager())
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
        config.with(PhysicsSystem())
        SilicaRegistries.BLOCKS_WITH_ENTITY.forEach {
            it.createProcessingSystem()?.let { system -> config.with(it.processingSystemPriority, system) }
        }
        SilicaRegistries.SYSTEM_REGISTRY.forEach {
            config.with(0 /* TODO: insert actual prio */, it)
        }
        SilicaRegistries.DYNAMIC_SYSTEM_REGISTRY.forEach {
            config.with(0 /* TODO: insert actual prio */, it(server))
        }
        config.with(Priority.HIGHEST, entityMap)
        config.with(Priority.LOWEST, EntityRemovalSystem())
        artemisWorld = ArtemisWorld(
            config.build()
                .registerAs<Entity3dMap>(entityMap)
                .registerAs<World>(this)
        )
        // vanilla doesn't like receiving a 0 id so we waste it
        artemisWorld.create()
        entityRemovalMapper = artemisWorld.getMapper()
    }

    override fun subsection(x: Int, y: Int, z: Int, w: Int, h: Int, d: Int): WorldSectionReader {
        return data[x, y, z, w, h, d]
    }

    override fun nonAirInChunk(x: Int, y: Int, z: Int): Int = data.nonAirInSection(x, y, z)

    override fun getBlockState(x: Int, y: Int, z: Int): BlockState = data[x, y, z]
    override fun getBlockState(pos: Position): BlockState = data[pos.x, pos.y, pos.z]

    private val blockArchetypesCache: Object2ObjectMap<Block, Archetype> = Object2ObjectOpenHashMap()
    private val entitiesArchetypesCache: Object2ObjectMap<EntityDefinition, Archetype> = Object2ObjectOpenHashMap()

    override fun setBlockState(pos: Position, state: BlockState, flag: WorldWriter.Flag): Boolean {
        if (isOutOfHeightLimit(pos)) return false
        val system = artemisWorld.getSystem<Entity3dMapSystem>()
        if (!system.getLiving(pos.toVec3f(), Vector3f(1f)).isEmpty) return false

        val existingBEs = system.getBlockEntities(pos)
        if (!existingBEs.isEmpty) {
            existingBEs.forEach {
                artemisWorld.delete(it)
            }
        }
        val block = state.block
        if (block is BlockEntityProvider) {
            artemisWorld.create(blockArchetypesCache.computeIfAbsent(block, Function {
                val builder = block.createArchetype()
                builder.add<BlockPositionComponent>()
                BlockEvents.INITIALIZE_ARCHETYPE_EVENT.dispatcher?.invoke(block, builder)
                builder.build(artemisWorld, "block:${block.identifier}")
            }))
        }
        val oldState = data[pos.x, pos.y, pos.z]
        data[pos.x, pos.y, pos.z] = state

        if (WorldWriter.Flag.NOTIFY_NEIGHBORS in flag) {
            Direction.ALL.forEach { updateNeighbor(pos, state, it.opposite, pos.shift(it)) }
        }

        if (WorldWriter.Flag.NOTIFY_LISTENERS in flag) {
            WorldEvents.REPLACE_BLOCKS_EVENT.dispatcher?.invoke(pos, oldState, state, flag)
        }
        return true
    }

    override fun spawnEntity(entity: EntityDefinition, editor: (EntityEdit) -> Unit) {
        val id = artemisWorld.create(entitiesArchetypesCache.computeIfAbsent(entity, Function {
            val archetype = it.createArchetype()
            EntityEvents.INITIALIZE_ARCHETYPE_EVENT.dispatcher?.invoke(it, archetype)
            archetype.add<EntityIdentity>()
                .build(artemisWorld, "entity:${entity.identifier}")
        }))

        artemisWorld.edit(id).also {
            val identity = it.create<EntityIdentity>()
            identity.uuid = UUID.randomUUID()
            identity.entityDefinition = entity
            editor(it)
            EntityEvents.SPAWN_ENTITY_EVENT.dispatcher?.invoke(it.entity)
        }
    }

    override fun updateEntity(id: Int, update: (Entity) -> Unit) {
        val entity = artemisWorld.getEntity(id)?.takeIf(Entity::isActive) ?: return
        update(entity)
    }

    override fun killEntity(id: Int) {
        entityRemovalMapper.set(id, true)
    }

    override fun saveWorld() {
        data.persist()
    }

    private fun updateNeighbor(pos: Position, state: BlockState, opposite: Direction, neighbor: Position) {
        val neighborState = getBlockState(neighbor)

        neighborState.block.onNeighborUpdate(this, neighbor, neighborState, pos, state, opposite)
    }

    override fun getFluidState(pos: Position): FluidState {
        TODO("Not yet implemented")
    }

    companion object {
        lateinit var worldGenerator: WorldGenerator
        private val WORLD_MIN
            get() = worldGenerator.minWorldWidth
        private val WORLD_MAX
            get() = worldGenerator.maxWorldWidth
        private val WORLD_SIZE
            get() = -WORLD_MIN + WORLD_MAX + 1

        fun actor(side: Side, server: SilicaServer): Behavior<World.Command> = Behaviors.setup {
            Actor(SilicaWorld(side, server), it)
        }
    }

    object Command : World.Command {
        class Tick(val delta: Float, val replyTo: ActorRef<Tock>) : World.Command {
            class Tock(val done: ActorRef<World.Command>)
        }

        /**
         * To be avoided when possible
         */
        open class PerformSilica(body: (SilicaWorld) -> Unit) : World.Command.DelayedCommand<SilicaWorld, Unit>(body)

        /**
         * To be avoided when possible
         */
        class AskSilica<T : Any>(val replyTo: ActorRef<T>, body: (SilicaWorld) -> T) :
            World.Command.DelayedCommand<SilicaWorld, T>(body)

//            inline fun <T : Any> createPlayer(
//                gameProfile: GameProfile,
//                replyTo: ActorRef<T>,
//                crossinline transform: (org.sandboxpowered.silica.vanilla.network.ecs.VanillaPlayerInput, PlayerInventoryComponent, Array<GameProfile>) -> T
//            ) = AskSilica(replyTo) {
//                val playerManager = it.artemisWorld.getSystem<SilicaPlayerManager>()
//                transform(
//                    playerManager.create(gameProfile),
//                    playerManager.createInventory(gameProfile),
//                    playerManager.getOnlinePlayerProfiles()
//                )
//            }
//
//            fun spawnEntity(
//                entityDefinition: EntityDefinition, initialize: (EntityEdit) -> Unit = { }
//            ) = PerformSilica {
//                it.spawnEntity(entityDefinition, initialize)
//            }
    }

    private class Actor(private val world: SilicaWorld, context: ActorContext<World.Command>) :
        AbstractBehavior<World.Command>(context), WithContext<World.Command> {

        private val commandQueue: Deque<World.Command.DelayedCommand<*, *>> = LinkedList()
        private val generator: ActorRef<TerrainGenerator> =
            context.spawn(TerrainGenerator.actor(), "terrain_generator")
        private val persistence: ActorRef<WorldStorage> = context.spawn(
            WorldStorage.actor((InternalAPI.instance as SilicaInternalAPI).networkAdapter!!.mapper),
            "world_persistence"
        )
        private var generated = false

        init {
            world.data = WorldData(
                Bounds().set(
                    worldGenerator.minWorldWidth, worldGenerator.minWorldHeight, worldGenerator.minWorldWidth,
                    worldGenerator.width, worldGenerator.height, worldGenerator.width
                ), Registries.BLOCKS[Identifier("air")].get().defaultState,
                context.system.scheduler(),
                persistence, generator
            )
        }

        override fun createReceive(): Receive<World.Command> = newReceiveBuilder()
            .onMessage(this::handleTick)
            .onMessage(this::handleAsk)
            .onMessage(this::handleDelayedCommand)
            .build()

        private fun handleTick(tick: Command.Tick): Behavior<World.Command> {
            if (!generated) {
                enqueueGeneration()
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
                        is World.Command.DelayedCommand.Perform -> next.body(world)
                        is Command.PerformSilica -> next.body(world)
                        is Command.AskSilica<*> -> {
                            @Suppress("UNCHECKED_CAST")
                            val replyTo = next.replyTo as ActorRef<Any>
                            replyTo.tell(next.body(world))
                        }
                        is World.Command.DelayedCommand.Ask<*> -> {
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

        private fun handleDelayedCommand(command: World.Command.DelayedCommand<*, *>): Behavior<World.Command> {
            commandQueue += command
            return Behaviors.same()
        }

        private fun handleAsk(ask: World.Command.Ask<Any>): Behavior<World.Command> {
            ask.replyTo.tell(ask.body(world))
            return Behaviors.same()
        }

        // TODO: TMP !!
        private fun enqueueGeneration() {
            world.data.load(Bounds().set(-99, 0, -99, 200, 200, 200))
        }
    }
}

private fun ImmutableIntBag<Any>.forEach(block: (Int) -> Unit) {
    for (i in 0 until this.size()) {
        block(this.get(i))
    }
}
