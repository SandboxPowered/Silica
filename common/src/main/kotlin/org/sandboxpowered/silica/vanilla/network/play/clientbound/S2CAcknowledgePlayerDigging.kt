package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CAcknowledgePlayerDigging(
    private val pos: Position,
    private val blockState: Int,
    private val status: Int,
    private val success: Boolean,
) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readPosition(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean())

    override fun write(buf: PacketByteBuf) {
        buf.writePosition(pos)
        buf.writeVarInt(blockState)
        buf.writeVarInt(status)
        buf.writeBoolean(success)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}