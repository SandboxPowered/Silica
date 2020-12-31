package org.sandboxpowered.silica.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class LengthPrepender extends MessageToByteEncoder<ByteBuf> {
    public static int getVarIntSize(int value) {
        for(int variable = 1; variable < 5; ++variable) {
            if ((value & -1 << variable * 7) == 0) {
                return variable;
            }
        }

        return 5;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        int packetSize = in.readableBytes();
        int length = getVarIntSize(packetSize);
        if (length > 3) {
            throw new IllegalArgumentException("unable to fit " + packetSize + " into 3");
        } else {
            PacketByteBuf packetByteBuf = new PacketByteBuf(out);
            packetByteBuf.ensureWritable(length + packetSize);
            packetByteBuf.writeVarInt(packetSize);
            packetByteBuf.writeBytes(in, in.readerIndex(), packetSize);
        }
    }
}
