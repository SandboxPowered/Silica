package org.sandboxpowered.silica.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder

class PacketDecoder(private val networkFlow: NetworkFlow) : ByteToMessageDecoder() {
    override fun decode(ctx: ChannelHandlerContext, inBuf: ByteBuf, out: MutableList<Any>) {
        if (inBuf.readableBytes() != 0) {
            val buf = PacketByteBuf(inBuf)
            val packetId = buf.readVarInt()
            val protocol = ctx.channel().attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get()
            val packet = protocol.createPacket(networkFlow, packetId)
            if (packet != null) {
                packet.read(buf)
                out.add(packet)
            } else {
//                throw new IOException("Unknown packet " + packetId);
            }
        }
    }
}