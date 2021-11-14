package org.sandboxpowered.silica.vanilla.network.netty

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import org.sandboxpowered.silica.vanilla.network.NetworkFlow
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.Protocol

class PacketDecoder(private val networkFlow: NetworkFlow) : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, inBuf: ByteBuf, out: MutableList<Any>) {
        if (inBuf.readableBytes() != 0) {
            val buf = PacketByteBuf(inBuf)
            val packetId = buf.readVarInt()
            val protocol = ctx.channel().attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get()
            val packet = protocol.createPacket(networkFlow, packetId, buf)
            require(packet != null) { "Unknown packet 0x${"%02x".format(packetId)}" }
            out.add(packet)
        }
    }
}