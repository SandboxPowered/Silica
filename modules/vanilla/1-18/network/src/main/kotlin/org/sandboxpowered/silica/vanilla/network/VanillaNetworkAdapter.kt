package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.artemis.Entity
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
import net.kyori.adventure.text.minimessage.MiniMessage
import org.joml.Vector3dc
import org.joml.Vector3f
import org.sandboxpowered.silica.api.ecs.component.EntityIdentity
import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.ecs.component.RotationComponent
import org.sandboxpowered.silica.api.ecs.component.VelocityComponent
import org.sandboxpowered.silica.api.entity.EntityEvents
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.network.Packet
import org.sandboxpowered.silica.api.server.PlayerManager
import org.sandboxpowered.silica.api.server.Server
import org.sandboxpowered.silica.api.util.extensions.*
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.api.world.WorldEvents
import org.sandboxpowered.silica.api.world.WorldWriter
import org.sandboxpowered.silica.api.world.persistence.BlockStateMapping
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.network.ecs.component.VanillaPlayerInputComponent
import org.sandboxpowered.silica.vanilla.network.netty.LengthPrepender
import org.sandboxpowered.silica.vanilla.network.netty.LengthSplitter
import org.sandboxpowered.silica.vanilla.network.netty.PacketDecoder
import org.sandboxpowered.silica.vanilla.network.netty.PacketEncoder
import org.sandboxpowered.silica.vanilla.network.packets.play.clientbound.*
import org.sandboxpowered.silica.vanilla.network.util.NetworkFlow
import org.sandboxpowered.silica.vanilla.network.util.mapping.BlockStateProtocolMapping
import org.sandboxpowered.silica.vanilla.network.util.mapping.VanillaProtocolMapping
import org.sandboxpowered.silica.api.Identifier
import org.sandboxpowered.silica.api.math.times
import java.util.*
import kotlin.random.Random

const val PROTOCOL_VERSION = 58 + 0x40000000

object VanillaNetworkAdapter : NetworkAdapter {
    override val id: Identifier = Identifier("minecraft", "1.18")

    override val protocol: Identifier = Identifier("minecraft", PROTOCOL_VERSION.toString())

    override val mapper: BlockStateMapping get() = BlockStateProtocolMapping.INSTANCE

    override fun createBehavior(server: Server): Behavior<NetworkAdapter.Command> = Behaviors.setup {
        VanillaNetworkBehavior(server, it)
    }

    sealed interface VanillaCommand : NetworkAdapter.Command {
        class CreateConnection(
            val profile: GameProfile,
            val handler: PacketHandler,
            val replyTo: ActorRef<in Boolean>
        ) : VanillaCommand

        class QueryMotd(val replyTo: ActorRef<in String>) : VanillaCommand
        class UpdateMotd(val mutation: (MOTD) -> Unit) : VanillaCommand

        class Disconnected(val user: GameProfile) : VanillaCommand

        class SendTo(val target: UUID, val packet: Packet) : VanillaCommand
        class SendToAll(val packet: Packet) : VanillaCommand
        class SendToAllExcept(val except: UUID, val packet: Packet) : VanillaCommand
        class SendToNearby(val position: Position, val distance: Int, val packet: Packet) : VanillaCommand
        class SendToWatching(val position: Position, val packet: Packet) : VanillaCommand
    }
}

private class VanillaNetworkBehavior(
    val server: Server,
    context: ActorContext<NetworkAdapter.Command>
) : AbstractBehavior<NetworkAdapter.Command>(context) {
    private val connections: Object2ObjectMap<UUID, ActorRef<PlayConnection>> = Object2ObjectOpenHashMap()

    private class ReceivePlayers(val playerManager: PlayerManager) : VanillaNetworkAdapter.VanillaCommand

    private lateinit var playerManager: PlayerManager

    private lateinit var motd: MOTD

    private lateinit var motdCache: String

    private val gson = GsonBuilder()
        .registerTypeAdapter(MOTDSerializer())
        .create()

    private fun updateMotdCache() {
        motdCache = gson.toJson(motd)
    }

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
        .onMessage(this::handleUpdateMotd)
        .onMessage(this::handleQueryMotd)
        .onMessage(this::handlePlayerManager)
        .build()

    private val vanillaWorld = context.spawn(
        VanillaWorldAdapter.actor(server.world, BlockStateProtocolMapping.INSTANCE),
        "vanilla-world-adapter"
    )

    init {
        EntityEvents.SPAWN_ENTITY_EVENT.subscribe(this::spawnEntity)
        EntityEvents.ENTITY_POSITION_EVENT.subscribe(this::updateEntityPosition)
        EntityEvents.ENTITY_VELOCITY_EVENT.subscribe(this::updateEntityVelocity)
        EntityEvents.REMOVE_ENTITIES_EVENT.subscribe(this::removeEntities)
        WorldEvents.REPLACE_BLOCKS_EVENT.subscribe(this::changeBlock)
        server.world.tell(World.Command.DelayedCommand.Ask(context.self) { ReceivePlayers(it.playerManager) })
    }

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

    private fun handleSendTo(send: VanillaNetworkAdapter.VanillaCommand.SendTo): Behavior<NetworkAdapter.Command> {
        connections[send.target]?.tell(PlayConnection.SendPacket(send.packet))

        return Behaviors.same()
    }

    private fun handleSendToAll(send: VanillaNetworkAdapter.VanillaCommand.SendToAll): Behavior<NetworkAdapter.Command> {
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToAllExcept(send: VanillaNetworkAdapter.VanillaCommand.SendToAllExcept): Behavior<NetworkAdapter.Command> {
        connections.forEach { (k, v) ->
            if (k != send.except)
                v.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToNearby(send: VanillaNetworkAdapter.VanillaCommand.SendToNearby): Behavior<NetworkAdapter.Command> {
        //TODO only send to connections within distance.
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleSendToWatching(send: VanillaNetworkAdapter.VanillaCommand.SendToWatching): Behavior<NetworkAdapter.Command> {
        //TODO only send to connections with a registered interest in the position.
        connections.values.forEach {
            it.tell(PlayConnection.SendPacket(send.packet))
        }

        return Behaviors.same()
    }

    private fun handleStart(start: NetworkAdapter.Command.Start): Behavior<NetworkAdapter.Command> {
        this.motd = MOTD(
            Version("1.18.2 - Vanilla", PROTOCOL_VERSION),
            players = Players(server.properties.maxPlayers, 0, mutableListOf()),
            // MiniMessage doesn't support MD anymore
//            description = MiniMessage.miniMessage().deserialize(server.properties.motd),
            description = MiniMessage.miniMessage().deserialize(server.properties.motd),
            favicon = "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAACxklEQVR4nO2av24TQRDGv7NP8Z8oSiQcR5cGB11JQ0EKKHgARKQg0SAeAQlegJI+Lc8QCaQg6CidmhpFSkhnJ0hRXES2Yplqw3nji+92Zm4Oeb/uzr692d/N7cy3dhBF0QQLrIp2ANryALQD0JYHoB2AtjwA7QC05QFoB6AtD0A7AG2FRd6sG8dO1z09OmKO5J8CSTPUDkN86XTYx+UEIgLgoNPBvVA+uThAsK8B3TguZPIA8H5lhTwGK4APa2ucwxUi1kc1GI+x3+vdHL/a2OAcXkSiuZqEAfACscd2VaFlkBvI3mBAuh4QqAJPajVsLy3lvu7F+jrqlWxLkgHJAYCcAaa5MSXpcDjE4XB483nWlfrr2dnU8bzs+HV9nSfMVJEzIK27S6vRrqUrCWS/12N5+gARwF4U4fHy8tzv/R6N8Pr09Nb5EMBbRyClAPBpcxMPm83c11Gzg2vyAHEN+HFx4QTAfm0MEHtiHJ3ePBVaBtOUBwjn0wdKAsBWEsjO8TH+jMcAeFPfiASAqxTdpYOtralj7r0Bkhn6ORqxtaRZ1Y1j542VWWJxg0VDANx3l2yRG6FZK/XzVgvNapUybGZRXwnyIng1maARBFPnvp2fTx2X2RazmKG89ZobCCUL2NxgAOCdQ+Nyv17H9uoq6d6lAGCLw/RkVSkAmJ1gSReYplIA0LDF8+6RReIAkto9OUF/Rve402jggcNWuoGhDoD7J6+82UHxCKpmqAy2WGxLjKrvl5f42O/fOt+qVPDG2oWiZEBpAdi663VZCAC2uGwxyQ1qTZ7TfZIAaNhg7nuSq4AJ6GW7jarlCjklBZutDH62VmwOx1dEhon1AZLBc26OkqvAs1oNjxx+DHWRxK4wqx2W6tgkJm4k+i8xChDJSSclCuB/0ML/U9QD0A5AWx6AdgDa8gC0A9CWB6AdgLY8AO0AtPUXrV7219gkQOMAAAAASUVORK5CYII="
        )
        updateMotdCache()

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
            return Behaviors.stopped()
        }

        return Behaviors.same()
    }

    private fun handleCreateConnection(createConnection: VanillaNetworkAdapter.VanillaCommand.CreateConnection): Behavior<NetworkAdapter.Command> {
        // TODO: store ref & dispose of the actor
        context.log.info("Creating connection for ${createConnection.profile.id}")

        val ref = context.spawn(
            PlayConnection.actor(server, createConnection.handler, this.vanillaWorld),
            "connection-${createConnection.profile.id}"
        )
        ref.tell(PlayConnection.Login)

        connections[createConnection.profile.id] = ref
        createConnection.replyTo.tell(true)
        context.self.tell(VanillaNetworkAdapter.VanillaCommand.UpdateMotd { it.addPlayer(createConnection.profile) })

        return Behaviors.same()
    }

    private fun handleDisconnected(disconnected: VanillaNetworkAdapter.VanillaCommand.Disconnected): Behavior<NetworkAdapter.Command> {
        context.log.info("Removing connection for ${disconnected.user.id}")
        connections.remove(disconnected.user.id)
        context.self.tell(VanillaNetworkAdapter.VanillaCommand.UpdateMotd { it.removePlayer(disconnected.user) })
        context.self.tell(VanillaNetworkAdapter.VanillaCommand.SendToAll(S2CPlayerInfo.removePlayer(arrayOf(disconnected.user.id))))
        return Behaviors.same()
    }

    private fun handleUpdateMotd(updateMotd: VanillaNetworkAdapter.VanillaCommand.UpdateMotd): Behavior<NetworkAdapter.Command> {
        updateMotd.mutation(this.motd)
        this.updateMotdCache()

        return Behaviors.same()
    }

    private fun handleQueryMotd(queryMotd: VanillaNetworkAdapter.VanillaCommand.QueryMotd): Behavior<NetworkAdapter.Command> {
        queryMotd.replyTo.tell(motdCache)
        return Behaviors.same()
    }

    private val entityRegistry by lazy { VanillaProtocolMapping.INSTANCE["minecraft:entity_type"] }
    private val noRotation = RotationComponent().apply {
        yaw = 0f
        pitch = 0f
    }

    private fun spawnEntity(e: Entity) {
        val identity = e.getComponent<EntityIdentity>() ?: return
        val (x, y, z) = e.getComponent<PositionComponent>()?.pos ?: return
        val velocityComponent = e.getComponent<VelocityComponent>()
        val (vx, vy, vz) = velocityComponent?.let { it.direction * it.velocity } ?: Vector3f(0f)
        val rot = e.getComponent() ?: noRotation
        val type = entityRegistry[identity.entityDefinition!!.identifier]

        context.self.tell(
            VanillaNetworkAdapter.VanillaCommand.SendToAll(
                S2CSpawnLivingEntity(
                    e.id,
                    identity.uuid!!,
                    type,
                    x, y, z,
                    rot.yaw,
                    rot.pitch,
                    0,
                    (vx * 8000).toInt().toShort(),
                    (vy * 8000).toInt().toShort(),
                    (vz * 8000).toInt().toShort(),
                )
            )
        )

        context.self.tell(
            VanillaNetworkAdapter.VanillaCommand.SendToAll(
                S2CEntityMetadata(
                    e.id, listOf(
                        S2CEntityMetadata.Entry.customName(e.id.toString()),
                        S2CEntityMetadata.Entry.customNameVisible(true)
                    )
                )
            )
        )
    }

    private fun updateEntityPosition(id: Int, prevPos: Vector3dc, newPos: Vector3dc) {
        val delta = newPos - prevPos
        if (delta.lengthSquared() > 64) {
            val entity = playerManager.getEntity(id)
            val rotation = entity?.getComponent<RotationComponent>()
            context.self.tell(
                VanillaNetworkAdapter.VanillaCommand.SendToAll(
                    S2CTeleportEntity(
                        id,
                        newPos.x(), newPos.y(), newPos.z(),
                        rotation?.yaw ?: 0f, rotation?.pitch ?: 0f,
                        false
                    )
                )
            )
            entity?.getComponent<VanillaPlayerInputComponent>()?.let { player ->
                context.self.tell(
                    VanillaNetworkAdapter.VanillaCommand.SendTo(
                        player.gameProfile.id, S2CSetPlayerPositionAndLook(
                            newPos.x(), newPos.y(), newPos.z(),
                            rotation?.yaw ?: 0f, rotation?.pitch ?: 0f,
                            0, Random.nextInt()
                        )
                    )
                )
            }
        } else context.self.tell(
            VanillaNetworkAdapter.VanillaCommand.SendToAll(
                S2CUpdateEntityPosition(
                    id,
                    (delta.x() * 4096).toInt().toShort(),
                    (delta.y() * 4096).toInt().toShort(),
                    (delta.z() * 4096).toInt().toShort(),
                    false // TODO
                )
            )
        )
    }

    private fun updateEntityVelocity(id: Int, velo: Vector3dc) {
        val (vx, vy, vz) = velo
        context.self.tell(
            VanillaNetworkAdapter.VanillaCommand.SendToAll(
                S2CUpdateEntityVelocity(
                    id,
                    (vx * 8000).toInt().toShort(),
                    (vy * 8000).toInt().toShort(),
                    (vz * 8000).toInt().toShort(),
                )
            )
        )
    }

    private fun removeEntities(entities: IntArray) {
        context.self.tell(VanillaNetworkAdapter.VanillaCommand.SendToAll(S2CDestroyEntities(entities)))
    }

    private fun changeBlock(pos: Position, old: BlockState, new: BlockState, flag: WorldWriter.Flag) {
        context.self.tell(
            VanillaNetworkAdapter.VanillaCommand.SendToWatching(
                pos,
                S2CBlockChange(pos, BlockStateProtocolMapping.INSTANCE[new])
            )
        )
    }

    // TODO: maybe this should change the behaviour to enter ready state
    private fun handlePlayerManager(pm: ReceivePlayers): Behavior<NetworkAdapter.Command> {
        this.playerManager = pm.playerManager

        return Behaviors.same()
    }

}