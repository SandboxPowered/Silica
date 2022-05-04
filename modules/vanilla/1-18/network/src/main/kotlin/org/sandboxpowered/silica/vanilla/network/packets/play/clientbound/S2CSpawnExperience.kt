package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class S2CSpawnExperience(
    val entityId: Int,
    val posX: Double,
    val posY: Double,
    val posZ: Double,
    val count: Short
) : PacketPlay {

    constructor(buf: PacketBuffer) : this(
        buf.readVarInt(),
        buf.readDouble(),
        buf.readDouble(),
        buf.readDouble(),
        buf.readShort(),
    )

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(entityId)
        buf.writeDouble(posX)
        buf.writeDouble(posY)
        buf.writeDouble(posZ)
        buf.writeShort(count)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}