package org.sandboxpowered.silica.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder
import java.io.IOException

class PacketEncoder(private val networkFlow: NetworkFlow) : MessageToByteEncoder<PacketBase>() {

    override fun encode(ctx: ChannelHandlerContext, msg: PacketBase, out: ByteBuf) {
        val protocol = ctx.channel().attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get()
        val id = protocol.getPacketId(networkFlow, msg)
        if (id == -1) {
            throw IOException("Can't serialize unregistered packet")
        } else {
            val packetByteBuf = PacketByteBuf(out)
            packetByteBuf.writeVarInt(id)
            msg.write(packetByteBuf)
        }
    }
}