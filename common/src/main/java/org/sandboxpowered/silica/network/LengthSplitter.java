package org.sandboxpowered.silica.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;

import java.util.List;

public class LengthSplitter extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        buf.markReaderIndex();
        byte[] bs = new byte[3];

        for (int i = 0; i < bs.length; ++i) {
            if (!buf.isReadable()) {
                buf.resetReaderIndex();
                return;
            }
            bs[i] = buf.readByte();
            if (bs[i] >= 0) {
                PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.wrappedBuffer(bs));
                try {
                    int length = packetBuf.readVarInt();
                    if (buf.readableBytes() >= length) {
                        out.add(buf.readBytes(length));
                        return;
                    }

                    buf.resetReaderIndex();
                } finally {
                    packetBuf.release();
                }

                return;
            }
        }

        throw new CorruptedFrameException("length wider than 21-bit");
    }
}
