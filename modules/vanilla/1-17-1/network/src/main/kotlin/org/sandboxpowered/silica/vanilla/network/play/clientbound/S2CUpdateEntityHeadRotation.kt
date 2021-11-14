package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CUpdateEntityHeadRotation(
    var entityId: Int,
    var yaw: Float
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readVarInt(), buf.readByte() * 256f / 360f)

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(entityId)
        buf.writeByte((yaw % 360 / 360 * 256).toInt().toByte())
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}