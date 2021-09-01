package org.sandboxpowered.silica.network.play.clientbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext
import org.sandboxpowered.silica.util.math.Position

class AcknowledgePlayerDigging(
    private val pos: Position?,
    private val blockState: Int,
    private val status: Int,
    private val success: Boolean,
) : PacketPlay {

    constructor() : this(null, 0, 0, false)

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