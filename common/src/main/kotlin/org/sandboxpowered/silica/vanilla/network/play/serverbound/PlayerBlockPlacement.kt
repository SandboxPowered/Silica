package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.content.block.Blocks
import org.sandboxpowered.silica.util.content.Direction
import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

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
        context.mutateWorld {
            it.setBlockState(location!!.shift(Direction.byId(face)), Blocks.STONE.defaultState)
        }
    }
}