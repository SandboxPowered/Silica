package org.sandboxpowered.silica.network.play.clientbound

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import com.mojang.authlib.properties.PropertyMap
import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext
import java.util.*

class PlayerInfo(
    private var action: Int = -1,
    private var uuids: Array<UUID> = emptyArray(),
    private var names: Array<String> = emptyArray(),// Action 0
    private var propertyMaps: Array<PropertyMap> = emptyArray(),// Action 0
    private var gamemodes: IntArray = intArrayOf(),// Action 0,1
    private var pings: IntArray = intArrayOf(),// Action 0,2
) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        action = buf.readVarInt()
    }

    override fun write(buf: PacketByteBuf) {
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
        fun addPlayer(profiles: Array<GameProfile>, gamemodes: IntArray, pings: IntArray): PlayerInfo {
            val uuids = arrayOfNulls<UUID>(profiles.size)
            val names = arrayOfNulls<String>(profiles.size)
            val propertyMaps = arrayOfNulls<PropertyMap>(profiles.size)
            for (i in profiles.indices) {
                val profile = profiles[i]
                uuids[i] = profile.id
                names[i] = profile.name
                propertyMaps[i] = profile.properties
            }
            return PlayerInfo(
                0,
                uuids.requireNoNulls(),
                names.requireNoNulls(),
                propertyMaps.requireNoNulls(),
                gamemodes,
                pings
            )
        }

        fun updateLatency(uuids: Array<UUID?>, pings: IntArray): PlayerInfo {
            return PlayerInfo(2, uuids.requireNoNulls(), emptyArray(), emptyArray(), IntArray(0), pings)
        }

        fun removePlayer(uuids: Array<UUID?>): PlayerInfo {
            return PlayerInfo(4, uuids.requireNoNulls(), emptyArray(), emptyArray(), intArrayOf(), intArrayOf())
        }
    }
}