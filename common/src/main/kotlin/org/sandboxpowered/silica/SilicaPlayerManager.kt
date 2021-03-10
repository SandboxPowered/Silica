package org.sandboxpowered.silica

import com.artemis.*
import com.artemis.annotations.All
import com.artemis.annotations.One
import com.artemis.annotations.Wire
import com.artemis.utils.IntBag
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.*
import org.sandboxpowered.api.util.text.Text
import org.sandboxpowered.silica.component.PlayerComponent
import org.sandboxpowered.silica.component.PositionComponent
import java.net.SocketAddress
import java.util.*
import kotlin.reflect.KClass

@All(PlayerComponent::class, PositionComponent::class)
class SilicaPlayerManager(var maxPlayers: Int) : BaseEntitySystem() {
    private val uuidToEntityId: Object2IntFunction<UUID> =
        Object2IntOpenHashMap<UUID>().apply { defaultReturnValue(-1) }
    private val entityToUuid: Int2ObjectFunction<UUID> = Int2ObjectOpenHashMap()
    val onlinePlayers: ObjectSet<UUID> = ObjectOpenHashSet()
    val onlinePlayerProfiles: Object2ObjectMap<UUID,GameProfile> = Object2ObjectOpenHashMap()
    private val entitiesToDelete = IntBag()

    @Wire
    lateinit var playerComponentMapper: ComponentMapper<PlayerComponent>
    @Wire
    lateinit var positionComponentMapper: ComponentMapper<PositionComponent>

    fun checkDisconnectReason(address: SocketAddress, profile: GameProfile): Text? {
        if (profile.isLegacy)
            return Text.translatable("multiplayer.disconnect.not_whitelisted")
        return null
    }

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

        playerArchetype = builder.build(world, "player")
    }

    fun disconnect(profile: GameProfile) {
        onlinePlayers.remove(profile.id)
        onlinePlayerProfiles.remove(profile.id)

        entityToUuid.remove(uuidToEntityId.removeInt(profile.id))
    }

    fun create(profile: GameProfile): Int {
        val existing = uuidToEntityId.getInt(profile.id)
        if (existing != -1) return existing

        val id = world.create(playerArchetype)
        val player = playerComponentMapper.get(id)!!
        player.profile = profile

        onlinePlayers.add(profile.id)
        onlinePlayerProfiles.put(profile.id, profile)

        uuidToEntityId[profile.id] = id
        entityToUuid[id] = profile.id
        return id
    }

    fun getPosition(ent: Int): PositionComponent {
        return positionComponentMapper.get(ent)
    }

    fun getOnlinePlayers(): Array<UUID> {
        return onlinePlayers.toTypedArray()
    }

    fun getOnlinePlayerProfiles(): Array<GameProfile> {
        return onlinePlayerProfiles.values.toTypedArray()
    }
}

private inline fun <reified T : Component> ArchetypeBuilder.add() {
    add(T::class.java)
}

private operator fun <V> Int2ObjectFunction<V>.set(key: Int, value: V) {
    put(key, value)
}

private operator fun <K> Object2IntFunction<K>.set(key: K, value: Int) {
    put(key, value)
}
