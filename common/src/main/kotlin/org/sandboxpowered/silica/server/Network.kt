package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.mojang.authlib.GameProfile
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.vanilla.network.*
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CKeepAliveClient
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CPlayerInfo
import java.util.*

sealed class Network {
    class Tick(val delta: Float, val replyTo: ActorRef<Tock>) : Network() {
        class Tock(val done: ActorRef<Network>)
    }

    class Start(val replyTo: ActorRef<in Boolean>) : Network()
    class Disconnected(val user: GameProfile) : Network()
    class CreateConnection(
        val profile: GameProfile,
        val handler: PacketHandler,
        val replyTo: ActorRef<in Boolean>
    ) : Network()

    class SendTo(val target: UUID, val packet: PacketPlay) : Network()
    class SendToAll(val packet: PacketPlay) : Network()
    class SendToAllExcept(val except: UUID, val packet: PacketPlay) : Network()
    class SendToNearby(val position: Position, val distance: Int, val packet: PacketPlay) : Network()
    class SendToWatching(val position: Position, val packet: PacketPlay) : Network()

    companion object {
        fun actor(server: SilicaServer): Behavior<Network> = Behaviors.setup {
            DedicatedServerNetworkActor(server, it)
        }
    }
}

// TODO: clean shutdown
private class DedicatedServerNetworkActor(
    private val server: SilicaServer, // TODO: remove this one when I can
    context: ActorContext<Network>
) : AbstractBehavior<Network>(context) {
    private val logger = context.log
    private val connections: Object2ObjectMap<UUID, ActorRef<PlayConnection>> = Object2ObjectOpenHashMap()

    override fun createReceive(): Receive<Network> = newReceiveBuilder()
        .onMessage(this::handleTick)
        .onMessage(this::handleStart)
        .onMessage(this::handleCreateConnection)
        .onMessage(this::handleDisconnected)
        .onMessage(this::handleSendTo)
        .onMessage(this::handleSendToAll)
        .onMessage(this::handleSendToNearby)
        .onMessage(this::handleSendToWatching)
        .onMessage(this::handleSendToAllExcept)
        .build()

    private var ticks: Int = 0

    private fun handleTick(tick: Network.Tick): Behavior<Network> {
        var latencyPacket: S2CPlayerInfo? = null
        if (ticks % 20 == 0) {
            val uuids = connections.keys.toTypedArray()
            val pings = IntArray(uuids.size)
            uuids.forEachIndexed { index, uuid ->
                pings[index] = 1
            }
            latencyPacket = S2CPlayerInfo.updateLatency(
                uuids,
                pings
            )
        }
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(S2CKeepAliveClient(System.currentTimeMillis())))
            if (ticks % 20 == 0) {
                it.tell(PlayConnection.SendPacket(latencyPacket!!))
            }
        }

        ticks++

        tick.replyTo.tell(Network.Tick.Tock(context.self))
        return Behaviors.same()
    }

    private fun handleSendTo(send: Network.SendTo): Behavior<Network> {
        connections[send.target]?.tell(PlayConnection.SendPacket(send.packet))

        return Behaviors.same()
    }

    private fun handleSendToAll(send: Network.SendToAll): Behavior<Network> {
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToAllExcept(send: Network.SendToAllExcept): Behavior<Network> {
        connections.forEach { (k, v) ->
            if (k != send.except)
                v.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToNearby(send: Network.SendToNearby): Behavior<Network> {
        //TODO only send to connections within distance.
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToWatching(send: Network.SendToWatching): Behavior<Network> {
        //TODO only send to connections with a registered interest in the position.
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleStart(start: Network.Start): Behavior<Network> {
        val properties = server.properties!!
        val group: EventLoopGroup
        val kclass: Class<out ServerChannel>
        if (Epoll.isAvailable()) {
            logger.info("using epoll socket channel")
            kclass = EpollServerSocketChannel::class.java
            group = EpollEventLoopGroup()
        } else {
            logger.info("using nio socket channel")
            kclass = NioServerSocketChannel::class.java
            group = NioEventLoopGroup()
        }
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(group)
                .channel(kclass)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.config().setOption(ChannelOption.TCP_NODELAY, true)
                        ch.pipeline()
                            .addLast("timeout", ReadTimeoutHandler(30))
                            .addLast("splitter", LengthSplitter())
                            .addLast("decoder", PacketDecoder(NetworkFlow.SERVERBOUND))
                            .addLast("prepender", LengthPrepender())
                            .addLast("encoder", PacketEncoder(NetworkFlow.CLIENTBOUND))
                            .addLast(
                                "handler",
                                PacketHandler(Connection(server, context.self, context.system.scheduler()))
                            )
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
            val future =
                bootstrap.bind(properties.serverIp.ifEmpty { "0.0.0.0" }, properties.serverPort).syncUninterruptibly()
            future.addListener {
                if (it.isDone) {
                    when {
                        it.isSuccess -> start.replyTo.tell(true)
                        it.isCancelled -> logger.info("cancelled")
                        else -> logger.error("Error", it.cause())
                    }
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            group.shutdownGracefully()
        }

        return Behaviors.same()
    }

    private fun handleCreateConnection(createConnection: Network.CreateConnection): Behavior<Network> {
        // TODO: store ref & dispose of the actor
        logger.info("Creating connection")

        val ref = context.spawn(
            PlayConnection.actor(server, createConnection.handler),
            "connection-${createConnection.profile.id}"
        )
        ref.tell(PlayConnection.Login)

        connections[createConnection.profile.id] = ref
        createConnection.replyTo.tell(true)

        return Behaviors.same()
    }

    private fun handleDisconnected(disconnected: Network.Disconnected): Behavior<Network> {
        connections.remove(disconnected.user.id)
        return Behaviors.same()
    }
}