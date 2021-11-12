package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CUpdateEntityRotation(
    var entityId: Int = -1,
    var yaw: Float = 0f,
    var pitch: Float = 0f,
    var onGround: Boolean = false
) : PacketPlay {
    override fun read(buf: PacketBuffer) {
        TODO("Not yet implemented")
    }

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(entityId)
        buf.writeByte((yaw % 360 / 360 * 256).toInt().toByte())
        buf.writeByte((pitch % 360 / 360 * 256).toInt().toByte())
        buf.writeBoolean(onGround)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}