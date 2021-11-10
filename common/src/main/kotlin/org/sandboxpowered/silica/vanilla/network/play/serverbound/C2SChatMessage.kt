package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SChatMessage(private val message: String) : PacketPlay {

    constructor(buf: PacketByteBuf) : this(buf.readString(MAX_SIZE))

    override fun write(buf: PacketByteBuf) {
        buf.writeString(message.take(MAX_SIZE))
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle item usage
    }

    private companion object {
        private const val MAX_SIZE = 256
    }
}