package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2STabComplete(private val transactionId: Int, private val text: String) : PacketPlay {

    constructor(buf: PacketByteBuf) : this(buf.readVarInt(), buf.readString(32500 / 4))

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(transactionId)
        buf.writeString(text.take(32500 / 4))
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle tab completion
    }
}