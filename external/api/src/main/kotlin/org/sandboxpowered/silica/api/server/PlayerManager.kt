package org.sandboxpowered.silica.api.server

import com.artemis.Entity
import com.mojang.authlib.GameProfile
import java.util.*

interface PlayerManager {
    val onlinePlayers: Array<UUID>
    val onlinePlayerProfiles: Array<GameProfile>
    fun createPlayer(profile: GameProfile): Entity
    fun getPlayerId(uuid: UUID): Int
    fun disconnect(profile: GameProfile)

    fun getEntity(id: Int): Entity?
}