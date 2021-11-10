package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SPluginChannel(private var channel: Identifier, private var data: PacketByteBuf) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readIdentity(), buf.readBytes(MAX_SIZE))

    override fun write(buf: PacketByteBuf) {
        buf.writeIdentity(channel)
        buf.writeBytes(if (data.readableBytes() > MAX_SIZE) data.readSlice(MAX_SIZE) else data)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info("C2SPluginChannel($channel, ${data.readString()})")
    }

    private companion object {
        private const val MAX_SIZE = 32767
    }
}