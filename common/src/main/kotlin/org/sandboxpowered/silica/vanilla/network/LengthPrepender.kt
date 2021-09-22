package org.sandboxpowered.silica.vanilla.network

import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

class LengthPrepender : MessageToByteEncoder<ByteBuf>() {
    override fun encode(ctx: ChannelHandlerContext, buf: ByteBuf, out: ByteBuf) {
        val packetSize = buf.readableBytes()
        val length = getVarIntSize(packetSize)
        require(length <= 3) { "unable to fit $packetSize into 3 bytes" }
        val packetByteBuf = PacketByteBuf(out)
        packetByteBuf.ensureWritable(length + packetSize)
        packetByteBuf.writeVarInt(packetSize)
        packetByteBuf.writeBytes(buf, buf.readerIndex(), packetSize)
    }

    private fun getVarIntSize(value: Int): Int {
        for (variable in 1 until 5) {
            if (value and (-1 shl variable * 7) == 0) {
                return variable
            }
        }
        return 5
    }
}