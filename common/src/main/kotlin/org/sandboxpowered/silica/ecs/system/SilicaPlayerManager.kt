package org.sandboxpowered.silica.ecs.system

import com.artemis.Archetype
import com.artemis.ArchetypeBuilder
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.annotations.All
import com.artemis.annotations.Wire
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import org.sandboxpowered.silica.ecs.component.*
import org.sandboxpowered.silica.util.extensions.add
import org.sandboxpowered.silica.util.extensions.set
import java.util.*

@All(PlayerComponent::class, PositionComponent::class)
class SilicaPlayerManager(var maxPlayers: Int) : BaseEntitySystem() {
    private val uuidToEntityId: Object2IntFunction<UUID> = Object2IntOpenHashMap<UUID>()
        .apply { defaultReturnValue(UNKNOWN_ID) }
    private val entityToUuid: Int2ObjectFunction<UUID> = Int2ObjectOpenHashMap()
    val onlinePlayers: ObjectSet<UUID> = ObjectOpenHashSet()
    val onlinePlayerProfiles: Object2ObjectMap<UUID, GameProfile> = Object2ObjectOpenHashMap()

    @Wire
    private lateinit var playerMapper: ComponentMapper<PlayerComponent>

    @Wire
    private lateinit var positionMapper: ComponentMapper<PositionComponent>

    @Wire
    private lateinit var hitboxMapper: ComponentMapper<HitboxComponent>

    @Wire
    private lateinit var playerInputMapper: ComponentMapper<VanillaPlayerInput>

    @Wire
    private lateinit var inventoryMapper: ComponentMapper<PlayerInventoryComponent>

    @Wire
    private lateinit var removalMapper: ComponentMapper<MarkForRemovalComponent>

    override fun processSystem() {

    }

    fun getPlayerId(uuid: UUID): Int {
        return uuidToEntityId.getInt(uuid)
    }

    fun getPlayerId(profile: GameProfile): Int {
        return getPlayerId(profile.id)
    }

    private lateinit var playerArchetype: Archetype

    override fun initialize() {
        super.initialize()

        val builder = ArchetypeBuilder()

        builder.add<PlayerComponent>()
        builder.add<PositionComponent>()
        builder.add<RotationComponent>()
        builder.add<HitboxComponent>()
        builder.add<VanillaPlayerInput>()
        builder.add<PlayerInventoryComponent>()

        playerArchetype = builder.build(world, "player")
    }

    fun disconnect(profile: GameProfile) {
        onlinePlayers.remove(profile.id)
        onlinePlayerProfiles.remove(profile.id)

        val entityId = uuidToEntityId.removeInt(profile.id)
        entityToUuid.remove(entityId)
        removalMapper.create(entityId)
    }

    fun create(profile: GameProfile): VanillaPlayerInput {
        val existing = uuidToEntityId.getInt(profile.id)
        if (existing != UNKNOWN_ID) return playerInputMapper[existing]

        val id = world.create(playerArchetype)
        val player = playerMapper.get(id)!!
        player.profile = profile

        onlinePlayers.add(profile.id)
        onlinePlayerProfiles[profile.id] = profile

        uuidToEntityId[profile.id] = id
        entityToUuid[id] = profile.id

        val playerPosition = positionMapper[id]
        playerPosition.pos.set(8.0, 8.0, 8.0)

        hitboxMapper[id].hitbox.set(0.6, 1.8, 0.6)

        val playerInput = playerInputMapper[id]
        playerInput.initialize(id, profile)
        playerInput.wantedPosition.set(playerPosition.pos)

        return playerInput
    }

    fun getVanillaInput(ent: Int): VanillaPlayerInput {
        return playerInputMapper.get(ent)
    }

    fun getOnlinePlayers(): Array<UUID> {
        return onlinePlayers.toTypedArray()
    }

    fun getOnlinePlayerProfiles(): Array<GameProfile> {
        return onlinePlayerProfiles.values.toTypedArray()
    }

    fun createInventory(gameProfile: GameProfile): PlayerInventoryComponent {
        return inventoryMapper[uuidToEntityId.getInt(gameProfile.id)]
    }

    private companion object {
        private const val UNKNOWN_ID = -1
    }
}
