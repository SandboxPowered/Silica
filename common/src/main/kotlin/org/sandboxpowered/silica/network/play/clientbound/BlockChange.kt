package org.sandboxpowered.silica.network.play.clientbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext
import org.sandboxpowered.silica.util.math.Position

class BlockChange(var location: Position? = null, var blockId: Int = -1) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        TODO("Not yet implemented")
    }

    override fun write(buf: PacketByteBuf) {
        buf.writePosition(location!!)
        buf.writeVarInt(blockId)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}