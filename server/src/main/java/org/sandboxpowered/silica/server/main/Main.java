package org.sandboxpowered.silica.server.main;

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
import org.sandboxpowered.silica.server.DedicatedServer;
import org.sandboxpowered.silica.server.ServerProperties;

import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Main {

    public static void main(String[] args) throws InterruptedException, NoSuchAlgorithmException {
        new DedicatedServer();
    }
}