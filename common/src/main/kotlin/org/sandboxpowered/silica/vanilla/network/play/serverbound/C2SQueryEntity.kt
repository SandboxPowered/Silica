package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SQueryEntity(private val transactionId: Int, private val entityId: Int) : PacketPlay {

    constructor(buf: PacketByteBuf) : this(buf.readVarInt(), buf.readVarInt())

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(transactionId)
        buf.writeVarInt(entityId)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle query entity
    }
}