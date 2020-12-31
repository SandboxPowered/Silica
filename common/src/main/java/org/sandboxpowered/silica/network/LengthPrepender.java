package org.sandboxpowered.silica.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class LengthPrepender extends MessageToByteEncoder<ByteBuf> {
    public static int getVarIntSize(int i) {
        for(int j = 1; j < 5; ++j) {
            if ((i & -1 << j * 7) == 0) {
                return j;
            }
        }

        return 5;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        int i = in.readableBytes();
        int j = getVarIntSize(i);
        if (j > 3) {
            throw new IllegalArgumentException("unable to fit " + i + " into " + 3);
        } else {
            PacketByteBuf packetByteBuf = new PacketByteBuf(out);
            packetByteBuf.ensureWritable(j + i);
            packetByteBuf.writeVarInt(i);
            packetByteBuf.writeBytes(in, in.readerIndex(), i);
        }
    }
}
