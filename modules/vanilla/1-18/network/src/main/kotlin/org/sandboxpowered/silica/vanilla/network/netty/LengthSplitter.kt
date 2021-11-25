package org.sandboxpowered.silica.vanilla.network.netty

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.handler.codec.CorruptedFrameException
import org.sandboxpowered.silica.vanilla.network.util.PacketByteBuf

class LengthSplitter : ByteToMessageDecoder() {

    override fun decode(ctx: ChannelHandlerContext, buf: ByteBuf, out: MutableList<Any>) {
        buf.markReaderIndex()
        val bs = ByteArray(3)
        for (i in bs.indices) {
            if (!buf.isReadable) {
                buf.resetReaderIndex()
                return
            }
            bs[i] = buf.readByte()
            if (bs[i] >= 0) {
                val packetBuf = PacketByteBuf(Unpooled.wrappedBuffer(bs))
                try {
                    val length = packetBuf.readVarInt()
                    if (buf.readableBytes() < length) {
                        buf.resetReaderIndex()
                        return
                    }
                    out.add(buf.readBytes(length))
                } finally {
                    packetBuf.release()
                }
                return
            }
        }
        throw CorruptedFrameException("length wider than 21-bit")
    }
}