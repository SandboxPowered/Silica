package org.sandboxpowered.silica.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.sandboxpowered.silica.network.*;

import java.nio.file.Paths;

public class DedicatedServer extends SilicaServer {
    public DedicatedServer() {
        ServerProperties properties = ServerProperties.fromFile(Paths.get("server.properties"));

        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ch.pipeline()
                                    .addLast(new ReadTimeoutHandler(30))
                                    .addLast(new LengthSplitter())
                                    .addLast(new PacketDecoder(Flow.SERVERBOUND))
                                    .addLast(new LengthPrepender())
                                    .addLast(new PacketEncoder(Flow.CLIENTBOUND))
                                    .addLast(new PacketHandler(new Connection(DedicatedServer.this)));
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(properties.getServerPort()).sync();

            future.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}
