package org.sandboxpowered.silica.vanilla.network.play.clientbound

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import java.util.*

class S2CPlayerInfo(
    private var action: Int = -1,
    private var uuids: Array<UUID> = emptyArray(),
    private var names: Array<String> = emptyArray(),// Action 0
    private var propertyMaps: Array<PropertyMap> = emptyArray(),// Action 0
    private var gamemodes: IntArray = intArrayOf(),// Action 0,1
    private var pings: IntArray = intArrayOf(),// Action 0,2
) : PacketPlay {
    override fun read(buf: PacketBuffer) {
        action = buf.readVarInt()
    }

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(action)
        buf.writeVarInt(uuids.size)
        for (i in uuids.indices) {
            buf.writeUUID(uuids[i])
            when (action) {
                0 -> {
                    buf.writeString(names[i], 16)
                    val map = propertyMaps[i]
                    buf.writeVarInt(map.size())
                    map.forEach { _, property: Property ->
                        buf.writeString(property.name)
                        buf.writeString(property.value)
                        buf.writeBoolean(property.hasSignature())
                        if (property.hasSignature()) {
                            buf.writeString(property.signature)
                        }
                    }
                    buf.writeVarInt(gamemodes[i])
                    buf.writeVarInt(pings[i])
                    buf.writeBoolean(false)
                }
                1 -> buf.writeVarInt(gamemodes[i])
                2 -> buf.writeVarInt(pings[i])
                3 -> buf.writeBoolean(false)
            }
        }
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}

    companion object {
        fun addPlayer(profiles: Array<GameProfile>, gamemodes: IntArray, pings: IntArray): S2CPlayerInfo {
            val uuids = arrayOfNulls<UUID>(profiles.size)
            val names = arrayOfNulls<String>(profiles.size)
            val propertyMaps = arrayOfNulls<PropertyMap>(profiles.size)
            for (i in profiles.indices) {
                val profile = profiles[i]
                uuids[i] = profile.id
                names[i] = profile.name
                propertyMaps[i] = profile.properties
            }
            return S2CPlayerInfo(
                0,
                uuids.requireNoNulls(),
                names.requireNoNulls(),
                propertyMaps.requireNoNulls(),
                gamemodes,
                pings
            )
        }

        fun updateLatency(uuids: Array<UUID?>, pings: IntArray): S2CPlayerInfo {
            return S2CPlayerInfo(2, uuids.requireNoNulls(), emptyArray(), emptyArray(), IntArray(0), pings)
        }

        fun removePlayer(uuids: Array<UUID?>): S2CPlayerInfo {
            return S2CPlayerInfo(4, uuids.requireNoNulls(), emptyArray(), emptyArray(), intArrayOf(), intArrayOf())
        }
    }
}