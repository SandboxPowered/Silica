package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.util.math.Position

class AcknowledgePlayerDigging(
    private val pos: Position? = null,
    private val blockState: Int = 0,
    private val status: Int = 0,
    private val success: Boolean = false,
) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        TODO("Not yet implemented")
    }

    override fun write(buf: PacketByteBuf) {
        buf.writePosition(pos!!)
        buf.writeVarInt(blockState)
        buf.writeVarInt(status)
        buf.writeBoolean(success)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}