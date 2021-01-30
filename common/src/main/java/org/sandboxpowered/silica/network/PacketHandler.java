package org.sandboxpowered.silica.network;

import io.netty.channel.*;
import org.sandboxpowered.silica.server.SilicaServer;

import java.net.SocketAddress;

public class PacketHandler extends SimpleChannelInboundHandler<Packet> {
    private final SilicaServer server;
    private final Connection connection;
    private Channel channel;
    private SocketAddress address;

    public PacketHandler(Connection connection) {
        this.connection = connection;
        this.server = connection.getServer();
        connection.setPacketHandler(this);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.channel = ctx.channel();
        this.address = this.channel.remoteAddress();

        setProtocol(Protocol.HANDSHAKE);
    }

    public void setProtocol(Protocol connectionProtocol) {
        this.channel.attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).set(connectionProtocol);
        this.channel.config().setAutoRead(true);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Packet msg) {
        if (this.channel.isOpen()) {
            msg.handle(this, connection);
        }
    }

    public void sendPacket(Packet packet) {
        Protocol wantedProtocol = Protocol.getProtocolForPacket(packet);
        Protocol currentProtocol = channel.attr(Protocol.PROTOCOL_ATTRIBUTE_KEY).get();
        if (wantedProtocol != currentProtocol) {
            this.channel.config().setAutoRead(false);
        }
        if (this.channel.eventLoop().inEventLoop()) {
            sendPacketInternal(packet, wantedProtocol, currentProtocol);
        } else {
            channel.eventLoop().execute(() -> sendPacketInternal(packet, wantedProtocol, currentProtocol));
        }
    }

    private void sendPacketInternal(Packet packet, Protocol wantedProtocol, Protocol currentProtocol) {
        if (wantedProtocol != currentProtocol) {
            setProtocol(wantedProtocol);
        }

        ChannelFuture channelFuture = this.channel.writeAndFlush(packet);

        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
}
