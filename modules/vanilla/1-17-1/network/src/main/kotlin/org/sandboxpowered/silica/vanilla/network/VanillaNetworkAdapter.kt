package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.artemis.Entity
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
import org.sandboxpowered.silica.api.ecs.component.EntityIdentity
import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.ecs.component.RotationComponent
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.network.Packet
import org.sandboxpowered.silica.api.server.Server
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.extensions.*
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.WorldWriter
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.network.netty.LengthPrepender
import org.sandboxpowered.silica.vanilla.network.netty.LengthSplitter
import org.sandboxpowered.silica.vanilla.network.netty.PacketDecoder
import org.sandboxpowered.silica.vanilla.network.netty.PacketEncoder
import org.sandboxpowered.silica.vanilla.network.play.clientbound.*
import java.util.*
import kotlin.math.floor

object VanillaNetworkAdapter : NetworkAdapter {
    override val id: Identifier = Identifier("minecraft", "1.17.1")
    override val protocol: Identifier = Identifier("minecraft", "756")

    override fun createBehavior(server: Server): Behavior<NetworkAdapter.Command> = Behaviors.setup {
        VanillaNetworkBehavior(server, it)
    }
}

class VanillaNetworkBehavior(
    val server: Server,
    context: ActorContext<NetworkAdapter.Command>
) : AbstractBehavior<NetworkAdapter.Command>(context) {
    private val connections: Object2ObjectMap<UUID, ActorRef<PlayConnection>> = Object2ObjectOpenHashMap()

    override fun createReceive(): Receive<NetworkAdapter.Command> = newReceiveBuilder()
        .onMessage(this::handleTick)
        .onMessage(this::handleStart)
        .onMessage(this::handleCreateConnection)
        .onMessage(this::handleDisconnected)
        .onMessage(this::handleSendTo)
        .onMessage(this::handleSendToAll)
        .onMessage(this::handleSendToNearby)
        .onMessage(this::handleSendToWatching)
        .onMessage(this::handleSendToAllExcept)
//        .onMessage(this::handleUpdateMotd)
//        .onMessage(this::handleQueryMotd)
        .build()

    private val vanillaWorld = context.spawn(
        VanillaWorldAdapter.actor(server.world, StateMappingManager.INSTANCE),
        "vanilla-world-adapter"
    )

    private var ticks: Int = 0

    private fun handleTick(tick: NetworkAdapter.Command.Tick): Behavior<NetworkAdapter.Command> {
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

        tick.replyTo.tell(NetworkAdapter.Command.Tick.Tock(context.self))
        return Behaviors.same()
    }

    private fun handleSendTo(send: VanillaCommand.SendTo): Behavior<NetworkAdapter.Command> {
        connections[send.target]?.tell(PlayConnection.SendPacket(send.packet))

        return Behaviors.same()
    }

    private fun handleSendToAll(send: VanillaCommand.SendToAll): Behavior<NetworkAdapter.Command> {
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToAllExcept(send: VanillaCommand.SendToAllExcept): Behavior<NetworkAdapter.Command> {
        connections.forEach { (k, v) ->
            if (k != send.except)
                v.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToNearby(send: VanillaCommand.SendToNearby): Behavior<NetworkAdapter.Command> {
        //TODO only send to connections within distance.
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToWatching(send: VanillaCommand.SendToWatching): Behavior<NetworkAdapter.Command> {
        //TODO only send to connections with a registered interest in the position.
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleStart(start: NetworkAdapter.Command.Start): Behavior<NetworkAdapter.Command> {
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

    private fun handleCreateConnection(createConnection: VanillaCommand.CreateConnection): Behavior<NetworkAdapter.Command> {
        // TODO: store ref & dispose of the actor
        context.log.info("Creating connection for ${createConnection.profile.id}")

        val ref = context.spawn(
            PlayConnection.actor(server, createConnection.handler, this.vanillaWorld),
            "connection-${createConnection.profile.id}"
        )
        ref.tell(PlayConnection.Login)

        connections[createConnection.profile.id] = ref
        createConnection.replyTo.tell(true)
//        context.self.tell(NetworkAdapter.Command.UpdateMotd { it.addPlayer(createConnection.profile) })

        return Behaviors.same()
    }

    private fun handleDisconnected(disconnected: VanillaCommand.Disconnected): Behavior<NetworkAdapter.Command> {
        context.log.info("Removing connection for ${disconnected.user.id}")
        connections.remove(disconnected.user.id)
//        context.self.tell(NetworkAdapter.Command.UpdateMotd { it.removePlayer(disconnected.user) })
        context.self.tell(VanillaCommand.SendToAll(S2CPlayerInfo.removePlayer(arrayOf(disconnected.user.id))))
        return Behaviors.same()
    }

//    private fun handleUpdateMotd(updateMotd: NetworkAdapter.Command.UpdateMotd): Behavior<NetworkAdapter.Command> {
//        updateMotd.mutation(this.motd)
//        this.updateMotdCache()
//
//        return Behaviors.same()
//    }

//    private fun handleQueryMotd(queryMotd: VanillaNetwork.QueryMotd): Behavior<VanillaNetwork> {
//        queryMotd.replyTo.tell(motdCache)
//        return Behaviors.same()
//    }

    private val entityRegistry by lazy { VanillaProtocolMapping.INSTANCE["minecraft:entity_type"] }
    private val noRotation = RotationComponent().apply {
        yaw = 0f
        pitch = 0f
    }

    private fun angleToBytes(angle: Float) = floor(angle * 256f / 360f).toInt().toByte()

    fun spawnEntity(e: Entity) {
        val (x, y, z) = e.getComponent<PositionComponent>()?.pos ?: return
        val identity = e.getComponent<EntityIdentity>() ?: return
        val rot = e.getComponent() ?: noRotation
        val type = entityRegistry[identity.entityDefinition!!.identifier]

        context.self.tell(
            VanillaCommand.SendToAll(
                S2CSpawnLivingEntity(
                    e.id, identity.uuid!!, type, x, y, z, angleToBytes(rot.yaw), angleToBytes(rot.pitch), 0, 0, 0, 0
                )
            )
        )
    }

    fun removeEntities(entities: IntArray) {
        context.self.tell(VanillaCommand.SendToAll(S2CDestroyEntities(entities)))
    }

    fun changeBlock(pos: Position, old: BlockState, new: BlockState, flag: WorldWriter.Flag) {
        context.self.tell(VanillaCommand.SendToWatching(pos, S2CBlockChange(pos, StateMappingManager.INSTANCE[new])))
    }

    object VanillaCommand {
        class CreateConnection(
            val profile: GameProfile,
            val handler: PacketHandler,
            val replyTo: ActorRef<in Boolean>
        ) : NetworkAdapter.Command

        class Disconnected(val user: GameProfile) : NetworkAdapter.Command

        class SendTo(val target: UUID, val packet: Packet) : NetworkAdapter.Command
        class SendToAll(val packet: Packet) : NetworkAdapter.Command
        class SendToAllExcept(val except: UUID, val packet: Packet) : NetworkAdapter.Command
        class SendToNearby(val position: Position, val distance: Int, val packet: Packet) : NetworkAdapter.Command
        class SendToWatching(val position: Position, val packet: Packet) : NetworkAdapter.Command
    }
}