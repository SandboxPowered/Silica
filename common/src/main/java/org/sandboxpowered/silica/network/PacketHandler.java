package org.sandboxpowered.silica.network;

import akka.actor.typed.ActorRef;
import io.netty.channel.*;

import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.Queue;

public class PacketHandler extends SimpleChannelInboundHandler<PacketBase> {
    public final Connection connection;
    private final Queue<PacketPlay> waiting = new LinkedList<>();
    private ActorRef<PlayConnection.Command> playConnection;
    private Channel channel;
    private SocketAddress address;

    public PacketHandler(Connection connection) {
        this.connection = connection;
        connection.setPacketHandler(this);
    }

    public void setPlayConnection(ActorRef<PlayConnection.Command> playConnection) {
        this.playConnection = playConnection;
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
    protected void channelRead0(ChannelHandlerContext ctx, PacketBase msg) {
        if (this.channel.isOpen()) {
            if (msg instanceof PacketPlay) {
                if (connection == null) waiting.add((PacketPlay) msg);
                else {
                    if (waiting.size() > 0) {
                        PacketPlay read;
                        while ((read = waiting.poll()) != null)
                            playConnection.tell(new PlayConnection.Command.ReceivePacket(read));
                    }
                    playConnection.tell(new PlayConnection.Command.ReceivePacket((PacketPlay) msg));
                }
            } else ((Packet) msg).handle(this, connection);
        }
    }

    public void sendPacket(PacketBase packet) {
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

    private void sendPacketInternal(PacketBase packet, Protocol wantedProtocol, Protocol currentProtocol) {
        if (wantedProtocol != currentProtocol) {
            setProtocol(wantedProtocol);
        }

        ChannelFuture channelFuture = this.channel.writeAndFlush(packet);

        channelFuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
    }
}
