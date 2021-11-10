package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.util.getLogger
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SQueryBlock(private val transactionId: Int, private val position: Position) : PacketPlay {

    constructor(buf: PacketByteBuf) : this(buf.readVarInt(), buf.readPosition())

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(transactionId)
        buf.writePosition(position)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle query block
    }
}