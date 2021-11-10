package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.google.gson.GsonBuilder
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
import net.kyori.adventure.text.Component
import net.mostlyoriginal.api.event.common.Subscribe
import org.sandboxpowered.silica.ecs.events.RemoveEntitiesEvent
import org.sandboxpowered.silica.ecs.events.ReplaceBlockEvent
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.util.extensions.registerTypeAdapter
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.*
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CBlockChange
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CDestroyEntities
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CKeepAliveClient
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CPlayerInfo
import org.sandboxpowered.silica.world.SilicaWorld
import org.sandboxpowered.silica.world.VanillaWorldAdapter
import java.util.*

sealed class VanillaNetwork {
    class Tick(val delta: Float, val replyTo: ActorRef<Tock>) : VanillaNetwork() {
        class Tock(val done: ActorRef<VanillaNetwork>)
    }

    class Start(val replyTo: ActorRef<in Boolean>) : VanillaNetwork()
    class Disconnected(val user: GameProfile) : VanillaNetwork()
    class CreateConnection(
        val profile: GameProfile,
        val handler: PacketHandler,
        val replyTo: ActorRef<in Boolean>
    ) : VanillaNetwork()

    class SendTo(val target: UUID, val packet: PacketPlay) : VanillaNetwork()
    class SendToAll(val packet: PacketPlay) : VanillaNetwork()
    class SendToAllExcept(val except: UUID, val packet: PacketPlay) : VanillaNetwork()
    class SendToNearby(val position: Position, val distance: Int, val packet: PacketPlay) : VanillaNetwork()
    class SendToWatching(val position: Position, val packet: PacketPlay) : VanillaNetwork()

    class UpdateMotd(val mutation: (MOTD) -> Unit) : VanillaNetwork()
    class QueryMotd(val replyTo: ActorRef<in String>) : VanillaNetwork()

    companion object {
        fun actor(server: SilicaServer): Behavior<VanillaNetwork> = Behaviors.setup {
            VanillaNetworkActor(server, it)
        }
    }
}

// TODO: clean shutdown
private class VanillaNetworkActor(
    private val server: SilicaServer, // TODO: remove this one when I can
    context: ActorContext<VanillaNetwork>
) : AbstractBehavior<VanillaNetwork>(context) {
    private val connections: Object2ObjectMap<UUID, ActorRef<PlayConnection>> = Object2ObjectOpenHashMap()

    private val vanillaWorld = context.spawn(
        VanillaWorldAdapter.actor(server.world, server.stateRemapper),
        "vanilla-world-adapter"
    )

    private val gson = GsonBuilder()
        .registerTypeAdapter(MOTDDeserializer())
        .registerTypeAdapter(MOTDSerializer())
        .create()

    private val motd = MOTD(Version("Sandbox Silica", -1), Players(0, 0, ArrayList()), Component.empty(), "")
    private var motdCache: String = gson.toJson(motd)

    private fun updateMotdCache() {
        motdCache = gson.toJson(motd)
    }

    init {
        server.world.tell(SilicaWorld.Command.RegisterEventSubscriber(this))
    }

    override fun createReceive(): Receive<VanillaNetwork> = newReceiveBuilder()
        .onMessage(this::handleTick)
        .onMessage(this::handleStart)
        .onMessage(this::handleCreateConnection)
        .onMessage(this::handleDisconnected)
        .onMessage(this::handleSendTo)
        .onMessage(this::handleSendToAll)
        .onMessage(this::handleSendToNearby)
        .onMessage(this::handleSendToWatching)
        .onMessage(this::handleSendToAllExcept)
        .onMessage(this::handleUpdateMotd)
        .onMessage(this::handleQueryMotd)
        .build()

    private var ticks: Int = 0

    private fun handleTick(tick: VanillaNetwork.Tick): Behavior<VanillaNetwork> {
        var latencyPacket: S2CPlayerInfo? = null
        if (ticks % 20 == 0) {
            val uuids = connections.keys.toTypedArray()
            val pings = IntArray(uuids.size) { 1 }
            latencyPacket = S2CPlayerInfo.updateLatency(
                uuids,
                pings
            )
        }
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(S2CKeepAliveClient(System.currentTimeMillis())))
            if (latencyPacket != null) it.tell(PlayConnection.SendPacket(latencyPacket))
        }

        ticks++

        tick.replyTo.tell(VanillaNetwork.Tick.Tock(context.self))
        return Behaviors.same()
    }

    private fun handleSendTo(send: VanillaNetwork.SendTo): Behavior<VanillaNetwork> {
        connections[send.target]?.tell(PlayConnection.SendPacket(send.packet))

        return Behaviors.same()
    }

    private fun handleSendToAll(send: VanillaNetwork.SendToAll): Behavior<VanillaNetwork> {
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToAllExcept(send: VanillaNetwork.SendToAllExcept): Behavior<VanillaNetwork> {
        connections.forEach { (k, v) ->
            if (k != send.except)
                v.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToNearby(send: VanillaNetwork.SendToNearby): Behavior<VanillaNetwork> {
        //TODO only send to connections within distance.
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToWatching(send: VanillaNetwork.SendToWatching): Behavior<VanillaNetwork> {
        //TODO only send to connections with a registered interest in the position.
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleStart(start: VanillaNetwork.Start): Behavior<VanillaNetwork> {
        val properties = server.properties
        val group: EventLoopGroup
        val kclass: Class<out ServerChannel>
        if (Epoll.isAvailable()) {
            context.log.info("using epoll socket channel")
            kclass = EpollServerSocketChannel::class.java
            group = EpollEventLoopGroup()
        } else {
            context.log.info("using nio socket channel")
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
                        it.isCancelled -> context.log.info("cancelled")
                        else -> context.log.error("Error", it.cause())
                    }
                }
            }
        } catch (e: InterruptedException) {
            e.printStackTrace()
            group.shutdownGracefully()
        }

        return Behaviors.same()
    }

    private fun handleCreateConnection(createConnection: VanillaNetwork.CreateConnection): Behavior<VanillaNetwork> {
        // TODO: store ref & dispose of the actor
        context.log.info("Creating connection for ${createConnection.profile.id}")

        val ref = context.spawn(
            PlayConnection.actor(server, createConnection.handler, this.vanillaWorld),
            "connection-${createConnection.profile.id}"
        )
        ref.tell(PlayConnection.Login)

        connections[createConnection.profile.id] = ref
        createConnection.replyTo.tell(true)
        context.self.tell(VanillaNetwork.UpdateMotd { it.addPlayer(createConnection.profile) })

        return Behaviors.same()
    }

    private fun handleDisconnected(disconnected: VanillaNetwork.Disconnected): Behavior<VanillaNetwork> {
        context.log.info("Removing connection for ${disconnected.user.id}")
        connections.remove(disconnected.user.id)
        context.self.tell(VanillaNetwork.UpdateMotd { it.removePlayer(disconnected.user) })
        context.self.tell(VanillaNetwork.SendToAll(S2CPlayerInfo.removePlayer(arrayOf(disconnected.user.id))))
        return Behaviors.same()
    }

    private fun handleUpdateMotd(updateMotd: VanillaNetwork.UpdateMotd): Behavior<VanillaNetwork> {
        updateMotd.mutation(this.motd)
        this.updateMotdCache()

        return Behaviors.same()
    }

    private fun handleQueryMotd(queryMotd: VanillaNetwork.QueryMotd): Behavior<VanillaNetwork> {
        queryMotd.replyTo.tell(motdCache)
        return Behaviors.same()
    }

    @Subscribe
    fun removeEntities(event: RemoveEntitiesEvent) {
        context.self.tell(VanillaNetwork.SendToAll(S2CDestroyEntities(event.entityIds)))
    }

    @Subscribe
    fun changeBlock(event: ReplaceBlockEvent) {
        context.self.tell(
            VanillaNetwork.SendToWatching(event.pos, S2CBlockChange(event.pos, server.stateRemapper[event.newState]))
        )
    }
}