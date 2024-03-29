package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

data class C2STabComplete(private val transactionId: Int, private val text: String) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readVarInt(), buf.readString(32500 / 4))

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(transactionId)
        buf.writeString(text.take(32500 / 4))
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle tab completion
    }
}