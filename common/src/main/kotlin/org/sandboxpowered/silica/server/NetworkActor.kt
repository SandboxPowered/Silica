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
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import org.sandboxpowered.silica.network.*
import org.sandboxpowered.silica.network.play.clientbound.KeepAliveClient
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.world.SilicaWorld
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashMap

// TODO: clean shutdown
class NetworkActor(
    private val server: SilicaServer, // TODO: remove this one when I can
    context: ActorContext<Command>
) : AbstractBehavior<NetworkActor.Command>(context) {
    private val logger = context.log
    private val connections: HashMap<UUID, ActorRef<PlayConnection.Command>> = HashMap()

    companion object {
        fun actor(server: SilicaServer): Behavior<in Command> = Behaviors.setup {
            NetworkActor(server, it)
        }
    }

    sealed class Command {
        class Tick(val delta: Float, val replyTo: ActorRef<Tock>) : Command() {
            class Tock(val done: ActorRef<Command>)
        }
        class Start(val replyTo: ActorRef<in Boolean>) : Command()
        class Disconnected(val ref: String /*TODO*/) : Command()
        class CreateConnection(
            val profile: GameProfile,
            val handler: PacketHandler,
            val replyTo: ActorRef<in Boolean>
        ) : Command()
    }

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(this::handleTick)
        .onMessage(this::handleStart)
        .onMessage(this::handleCreateConnection)
        .build()

    private fun handleTick(tick: Command.Tick): Behavior<Command> {
        connections.values.forEach { it.tell(PlayConnection.Command.SendPacket(KeepAliveClient())) }
        tick.replyTo.tell(Command.Tick.Tock(context.self))
        return Behaviors.same()
    }

    private fun handleStart(start: Command.Start): Behavior<Command> {
        val properties = ServerProperties.fromFile(Paths.get("server.properties"))
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline()
                            .addLast(ReadTimeoutHandler(30))
                            .addLast(LengthSplitter())
                            .addLast(PacketDecoder(Flow.SERVERBOUND))
                            .addLast(LengthPrepender())
                            .addLast(PacketEncoder(Flow.CLIENTBOUND))
                            .addLast(PacketHandler(Connection(server, context.self, context.system.scheduler())))
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
            val future = bootstrap.bind(properties.serverIp.ifEmpty { "0.0.0.0" }, properties.serverPort)
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
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }

        return Behaviors.same()
    }

    private fun handleCreateConnection(createConnection: Command.CreateConnection): Behavior<Command> {
        // TODO: store ref & dispose of the actor
        logger.info("Creating connection")

        val ref = context.spawn(
            PlayConnection.actor(server, createConnection.handler),
            "connection-${createConnection.profile.id}"
        )
        ref.tell(PlayConnection.Command.Login)

        connections[createConnection.profile.id] = ref
        createConnection.replyTo.tell(true)

        return Behaviors.same()
    }
}