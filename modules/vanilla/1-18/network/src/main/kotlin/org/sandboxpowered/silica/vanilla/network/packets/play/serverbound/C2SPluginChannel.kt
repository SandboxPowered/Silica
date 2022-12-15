package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class C2SPluginChannel(private var channel: Identifier, private var data: PacketBuffer) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readIdentifier(), buf.readBytes(MAX_SIZE))

    override fun write(buf: PacketBuffer) {
        buf.writeIdentifier(channel)
        buf.writeBytes(if (data.readableBytes > MAX_SIZE) data.readSlice(MAX_SIZE) else data)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info("C2SPluginChannel($channel, ${data.readString()})")
    }

    private companion object {
        private const val MAX_SIZE = 32767
    }
}