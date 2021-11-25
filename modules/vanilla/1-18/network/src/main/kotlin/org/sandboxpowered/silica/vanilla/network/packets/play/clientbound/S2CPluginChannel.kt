package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import io.netty.buffer.Unpooled
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.util.PacketByteBuf

class S2CPluginChannel(private var channel: Identifier, private var data: PacketBuffer) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readIdentifier(), buf.readBytes(MAX_SIZE))

    override fun write(buf: PacketBuffer) {
        buf.writeIdentifier(channel)
        buf.writeBytes(if (data.readableBytes > MAX_SIZE) data.readSlice(MAX_SIZE) else data)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) = Unit

    companion object {
        private const val MAX_SIZE = 1048576

        inline operator fun invoke(channel: Identifier, data: (PacketByteBuf) -> Unit) =
            S2CPluginChannel(channel, PacketByteBuf(Unpooled.buffer()).apply(data))
    }
}