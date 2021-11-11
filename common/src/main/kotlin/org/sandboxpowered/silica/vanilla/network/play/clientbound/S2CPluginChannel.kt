package org.sandboxpowered.silica.vanilla.network.play.clientbound

import io.netty.buffer.Unpooled
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CPluginChannel(private var channel: Identifier, private var data: PacketByteBuf) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readIdentity(), buf.readBytes(MAX_SIZE))

    override fun write(buf: PacketByteBuf) {
        buf.writeIdentity(channel)
        buf.writeBytes(if (data.readableBytes() > MAX_SIZE) data.readSlice(MAX_SIZE) else data)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) = Unit

    companion object {
        private const val MAX_SIZE = 1048576

        inline operator fun invoke(channel: Identifier, data: (PacketByteBuf) -> Unit) =
            S2CPluginChannel(channel, PacketByteBuf(Unpooled.buffer()).apply(data))
    }
}