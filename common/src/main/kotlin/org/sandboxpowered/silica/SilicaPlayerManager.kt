package org.sandboxpowered.silica

import com.artemis.Archetype
import com.artemis.BaseEntitySystem
import com.artemis.ComponentMapper
import com.artemis.annotations.One
import com.artemis.annotations.Wire
import com.artemis.utils.IntBag
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntFunction
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import org.sandboxpowered.api.util.text.Text
import org.sandboxpowered.silica.component.PlayerComponent
import java.net.SocketAddress
import java.util.*

@One(PlayerComponent::class)
class SilicaPlayerManager(var maxPlayers: Int, var playerArchetype: Archetype) : BaseEntitySystem() {
    private val uuidToEntityId: Object2IntFunction<UUID> =
        Object2IntOpenHashMap<UUID>().apply { defaultReturnValue(-1) }
    private val entityToUuid: Int2ObjectFunction<UUID> = Int2ObjectOpenHashMap()
    private val entitiesToDelete = IntBag()

    @Wire
    lateinit var playerComponentMapper: ComponentMapper<PlayerComponent>

    fun checkDisconnectReason(address: SocketAddress, profile: GameProfile): Text? {
        if (profile.isLegacy)
            return Text.translatable("multiplayer.disconnect.not_whitelisted")
        return null
    }

    var onlinePlayers: Int = 0

    override fun processSystem() {
        onlinePlayers = 1
    }

    fun getPlayerId(uuid: UUID): Int {
        return uuidToEntityId.getInt(uuid)
    }

    fun getPlayerId(profile: GameProfile): Int {
        return getPlayerId(profile.id)
    }

    fun create(profile: GameProfile): Int {
        val existing = uuidToEntityId.getInt(profile.id)
        if (existing != -1) return existing

        val id = world.create(playerArchetype)
        val player = playerComponentMapper.get(id)!!
        player.profile = profile

        uuidToEntityId[profile.id] = id
        entityToUuid[id] = profile.id
        return id
    }
}

private operator fun <V> Int2ObjectFunction<V>.set(key: Int, value: V) {
    put(key, value)
}

private operator fun <K> Object2IntFunction<K>.set(key: K, value: Int) {
    put(key, value)
}
