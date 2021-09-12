package org.sandboxpowered.silica.network.play.serverbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext
import org.sandboxpowered.silica.util.math.Position

class PlayerBlockPlacement(
    var hand: Int = -1,
    var location: Position? = null,
    var face: Int = -1,
    var cursorX: Float = 0f,
    var cursorY: Float = 0f,
    var cursorZ: Float = 0f,
    var insideBlock: Boolean = false,
) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        hand = buf.readVarInt()
        location = buf.readPosition()
        face = buf.readVarInt()
        cursorX = buf.readFloat()
        cursorY = buf.readFloat()
        cursorZ = buf.readFloat()
        insideBlock = buf.readBoolean()
    }

    override fun write(buf: PacketByteBuf) {
        TODO("Not yet implemented")
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        // TODO
    }
}