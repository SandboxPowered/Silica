package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class UpdateEntityPosition(
    var entityId: Int = -1,
    var deltaX: Int = 0,
    var deltaY: Int = 0,
    var deltaZ: Int = 0,
    var onGround: Boolean = false
) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        TODO("Not yet implemented")
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(entityId)
        buf.writeShort(deltaX)
        buf.writeShort(deltaY)
        buf.writeShort(deltaZ)
        buf.writeBoolean(onGround)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}