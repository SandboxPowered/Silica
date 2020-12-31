package org.sandboxpowered.silica.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class PacketDecoder extends ByteToMessageDecoder {
    private final Flow flow;

    public PacketDecoder(Flow flow) {
        this.flow = flow;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() != 0) {
            PacketByteBuf buf = new PacketByteBuf(in);
            int packetId = buf.readVarInt();
            Protocol protocol = ctx.channel().attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get();
            Packet packet = protocol.createPacket(flow, packetId);
            packet.read(buf);
            out.add(packet);
        }
    }
}
