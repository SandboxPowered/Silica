package org.sandboxpowered.silica.ecs.system

import com.artemis.Archetype
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.Entity
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import org.sandboxpowered.silica.api.ecs.component.*
import org.sandboxpowered.silica.api.entity.BaseEntityDefinition
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.entity.EntityEvents
import org.sandboxpowered.silica.api.server.PlayerManager
import org.sandboxpowered.utilities.Identifier
import org.sandboxpowered.silica.api.util.extensions.add
import org.sandboxpowered.silica.api.util.extensions.set
import java.util.*

@All(PlayerComponent::class, PositionComponent::class)
class SilicaPlayerManager : BaseEntitySystem(), PlayerManager {
    private val uuidToEntityId: Object2IntFunction<UUID> = Object2IntOpenHashMap<UUID>()
        .apply { defaultReturnValue(UNKNOWN_ID) }
    private val entityToUuid: Int2ObjectFunction<UUID> = Int2ObjectOpenHashMap()
    val _onlinePlayers: ObjectSet<UUID> = ObjectOpenHashSet()
    val _onlinePlayerProfiles: Object2ObjectMap<UUID, GameProfile> = Object2ObjectOpenHashMap()
    override val onlinePlayers: Array<UUID>
        get() = _onlinePlayers.toTypedArray()
    override val onlinePlayerProfiles: Array<GameProfile>
        get() = _onlinePlayerProfiles.values.toTypedArray()

    @Wire
    private lateinit var playerMapper: ComponentMapper<PlayerComponent>

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>

    @Wire
    private lateinit var hitboxMapper: ComponentMapper<HitboxComponent>

    @Wire
    private lateinit var inventoryMapper: ComponentMapper<PlayerInventoryComponent>

    @Wire
    private lateinit var removalMapper: ComponentMapper<MarkForRemovalComponent>

    override fun processSystem() {

    }

    override fun getPlayerId(uuid: UUID): Int {
        return uuidToEntityId.getInt(uuid)
    }

    fun getPlayerId(profile: GameProfile): Int {
        return getPlayerId(profile.id)
    }

    private lateinit var playerEntDefinition: EntityDefinition

    private lateinit var playerArchetype: Archetype

    override fun initialize() {
        super.initialize()

        val entityDefinition = BaseEntityDefinition(Identifier("player")) {
            add<PlayerComponent>()
            add<PositionComponent>()
            add<RotationComponent>()
            add<HitboxComponent>()
            add<PlayerInventoryComponent>()
        }

        val arch = entityDefinition.createArchetype()
        EntityEvents.INITIALIZE_ARCHETYPE_EVENT.dispatcher?.invoke(entityDefinition, arch)

        playerArchetype = arch.build(world, "entity:${entityDefinition.identifier}")
    }

    override fun disconnect(profile: GameProfile) {
        _onlinePlayers.remove(profile.id)
        _onlinePlayerProfiles.remove(profile.id)

        val entityId = uuidToEntityId.removeInt(profile.id)
        entityToUuid.remove(entityId)
        removalMapper.create(entityId)
    }

    override fun createPlayer(profile: GameProfile): Entity {
        val existing = uuidToEntityId.getInt(profile.id)
        if (existing != UNKNOWN_ID) return getEntity(existing)!!

        val entity = world.createEntity(playerArchetype)
        val player = playerMapper.get(entity)!!
        player.profile = profile

        _onlinePlayers.add(profile.id)
        _onlinePlayerProfiles[profile.id] = profile

        uuidToEntityId[profile.id] = entity.id
        entityToUuid[entity.id] = profile.id

        val playerPosition = positionMapper[entity]
        playerPosition.pos.set(8.0, 10.0, 8.0)

        hitboxMapper[entity].hitbox.set(0.6, 1.8, 0.6)

        EntityEvents.SPAWN_ENTITY_EVENT.dispatcher?.invoke(entity)

//        val playerInput = playerInputMapper[id]
//        playerInput.initialize(id, profile)
//        playerInput.wantedPosition.set(playerPosition.pos)

        return entity
    }

    override fun getEntity(id: Int): Entity? = world.getEntity(id)

    fun createInventory(gameProfile: GameProfile): PlayerInventoryComponent =
        inventoryMapper[uuidToEntityId.getInt(gameProfile.id)]

    private companion object {
        private const val UNKNOWN_ID = -1
    }
}
