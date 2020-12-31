package org.sandboxpowered.silica.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.IOException;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
    private final Flow flow;

    public PacketEncoder(Flow flow) {
        this.flow = flow;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) throws Exception {
        Protocol protocol = ctx.channel().attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get();
        int id = protocol.getPacketId(this.flow, msg);
        if (id == -1) {
            throw new IOException("Can't serialize unregistered packet");
        } else {
            PacketByteBuf packetByteBuf = new PacketByteBuf(out);
            packetByteBuf.writeVarInt(id);
            msg.write(packetByteBuf);
        }
    }
}