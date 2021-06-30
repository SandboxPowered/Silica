package org.sandboxpowered.silica.network

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.artemis.BaseSystem
import com.mojang.authlib.GameProfile
import org.sandboxpowered.api.util.Identifier
import org.sandboxpowered.silica.SilicaPlayerManager
import org.sandboxpowered.silica.component.VanillaPlayerInput
import org.sandboxpowered.silica.nbt.CompoundTag
import org.sandboxpowered.silica.network.play.clientbound.*
import org.sandboxpowered.silica.server.Network
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.world.SilicaWorld
import org.sandboxpowered.silica.world.util.BlocTree
import java.time.Duration
import com.artemis.World as ArtemisWorld

sealed class PlayConnection {
    class ReceivePacket(val packet: PacketPlay) : PlayConnection()
    class SendPacket(val packet: PacketPlay) : PlayConnection()
    class ReceiveWorld(val blocks: BlocTree) : PlayConnection()
    class ReceivePlayer(val gameProfiles: Array<GameProfile>, val input: VanillaPlayerInput) : PlayConnection()
    class Disconnected(val profile: GameProfile) : PlayConnection()

    class FailedPlayerCreation(val reason: String) : PlayConnection()

    object Login : PlayConnection()

    companion object {
        fun actor(server: SilicaServer, packetHandler: PacketHandler): Behavior<PlayConnection> = Behaviors.setup {
            PlayConnectionActor(server, packetHandler, it)
        }
    }
}

private class PlayConnectionActor(
    private val server: SilicaServer,
    private val packetHandler: PacketHandler,
    context: ActorContext<PlayConnection>
) : AbstractBehavior<PlayConnection>(context) {

    override fun createReceive(): Receive<PlayConnection> = newReceiveBuilder()
        .onMessage(this::handleLoginStart)
        .onMessage(this::handleSend)
        .onMessage(this::handleReceive)
        .onMessage(this::handleReceivePlayer)
        .onMessage(this::handleFailedToCreatePlayer)
        .onMessage(this::handleReceiveWorld)
        .onMessage(this::handleDisconnected)
        .build()

    private val logger = context.log
    // TODO: apply at the right time + unsafe to keep a ref to a component
    private lateinit var playerInput: VanillaPlayerInput

    private val playContext by lazy { PlayContext {
        server.world.tell(SilicaWorld.Command.DelayedCommand.Perform { _ -> it(playerInput) })
    } }

    init {
        this.packetHandler.setPlayConnection(context.self)
    }

    private fun handleReceive(receive: PlayConnection.ReceivePacket): Behavior<PlayConnection> {
        receive.packet.handle(this.packetHandler, this.playContext)

        return Behaviors.same()
    }

    private fun handleSend(receive: PlayConnection.SendPacket): Behavior<PlayConnection> {
        packetHandler.sendPacket(receive.packet)

        return Behaviors.same()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleLoginStart(login: PlayConnection.Login): Behavior<PlayConnection> {
        context.ask(
            PlayConnection.ReceivePlayer::class.java, server.world, Duration.ofSeconds(1),
            {
                SilicaWorld.Command.DelayedCommand.createPlayer(
                    packetHandler.connection.profile,
                    it
                ) { input, profiles ->
                    PlayConnection.ReceivePlayer(profiles, input)
                }
            },
            { receive, throwable ->
                if (throwable != null) PlayConnection.FailedPlayerCreation("${throwable.javaClass.name}: ${throwable.message}")
                else receive
            }
        )
        return Behaviors.same()
    }

    private fun handleReceivePlayer(receive: PlayConnection.ReceivePlayer): Behavior<PlayConnection> {
        this.playerInput = receive.input
        val overworld = Identifier.of("minecraft", "overworld")
        val codec = CompoundTag()
        val dimReg = CompoundTag()
        dimReg.setString("type", "minecraft:dimension_type")
        val overworldTypeEntry = CompoundTag()
        overworldTypeEntry.setString("name", overworld.toString())
        overworldTypeEntry.setInt("id", 0)
        val overworldType = CompoundTag()
        overworldType.setBoolean("piglin_safe", false)
        overworldType.setBoolean("natural", true)
        overworldType.setFloat("ambient_light", 1f)
        overworldType.setString("infiniburn", "")
        overworldType.setBoolean("respawn_anchor_works", false)
        overworldType.setBoolean("has_skylight", true)
        overworldType.setBoolean("bed_works", true)
        overworldType.setString("effects", "minecraft:overworld")
        overworldType.setBoolean("has_raids", true)
        overworldType.setInt("logical_height", 256)
        overworldType.setFloat("coordinate_scale", 1f)
        overworldType.setBoolean("ultrawarm", false)
        overworldType.setBoolean("has_ceiling", false)
        overworldTypeEntry.setTag("element", overworldType)
        dimReg.setList("value", listOf(overworldTypeEntry))
        val biomeReg = CompoundTag()
        biomeReg.setString("type", "minecraft:worldgen/biome")
        val plainsBiomeEntry = CompoundTag()
        plainsBiomeEntry.setString("name", "minecraft:plains")
        plainsBiomeEntry.setInt("id", 0)
        val plains = CompoundTag()
        plains.setString("precipitation", "rain")
        plains.setFloat("depth", 0f)
        plains.setFloat("temperature", 0f)
        plains.setFloat("scale", 1f)
        plains.setFloat("downfall", 1f)
        plains.setString("category", "plains")
        val effects = CompoundTag()
        effects.setInt("sky_color", 8364543)
        effects.setInt("water_fog_color", 8364543)
        effects.setInt("fog_color", 8364543)
        effects.setInt("water_color", 8364543)
        plains.setTag("effects", effects)
        plainsBiomeEntry.setTag("element", plains)
        biomeReg.setList("value", listOf(plainsBiomeEntry))
        codec.setTag("minecraft:dimension_type", dimReg)
        codec.setTag("minecraft:worldgen/biome", biomeReg)
        packetHandler.sendPacket(
            JoinGame(
                0,
                false,
                1.toShort(),
                (-1).toShort(),
                1,
                arrayOf(overworld),
                codec,
                overworldType,
                overworld,
                0,
                20,
                4,
                false,
                true,
                false,
                true
            )
        )
        packetHandler.sendPacket(HeldItemChange(0.toByte()))
        packetHandler.sendPacket(DeclareRecipes())
        packetHandler.sendPacket(DeclareTags())
        packetHandler.sendPacket(EntityStatus())
        packetHandler.sendPacket(DeclareCommands())
        packetHandler.sendPacket(UnlockRecipes())
        val currentPos = receive.input.wantedPosition
        packetHandler.sendPacket(SetPlayerPositionAndLook(currentPos.x, currentPos.y, currentPos.z, 0f, 0f, 0.toByte(), 0))
        val gamemodes = IntArray(receive.gameProfiles.size)
        val pings = IntArray(receive.gameProfiles.size)
        receive.gameProfiles.forEachIndexed { index, uuid ->
            gamemodes[index] = 1
            pings[index] = 1
        }
        server.network.tell(
            Network.SendToAll(PlayerInfo.addPlayer(
            receive.gameProfiles,gamemodes,pings
        )))
        packetHandler.sendPacket(UpdateChunkPosition(0, 0))

        server.world.tell(
            SilicaWorld.Command.Ask(
                { PlayConnection.ReceiveWorld((it as SilicaWorld).getTerrain()) },
                context.self
            )
        )

        return Behaviors.same()
    }

    private fun handleDisconnected(disconnected: PlayConnection.Disconnected): Behavior<PlayConnection> {
        server.network.tell(Network.Disconnected(disconnected.profile))
        server.world.tell(SilicaWorld.Command.DelayedCommand.PerformSilica {
            it.artemisWorld.getSystem<SilicaPlayerManager>().disconnect(disconnected.profile)
        })
        return Behaviors.stopped()
    }

    private fun handleFailedToCreatePlayer(failure: PlayConnection.FailedPlayerCreation): Behavior<PlayConnection> {
        // TODO: disconnect
        logger.error("Could not create player: ${failure.reason}")
        return Behaviors.same()
    }

    private fun handleReceiveWorld(world: PlayConnection.ReceiveWorld): Behavior<PlayConnection> {
        logger.info("Sending world")
        for (x in -2..2) {
            for (z in -2..2) {
                val time = System.currentTimeMillis()
                packetHandler.sendPacket(ChunkData(x, z, world.blocks, server.stateManager::toVanillaId))
                logger.info("Took {}ms to create Chunk Data for {} {}", System.currentTimeMillis() - time, x, z)
                packetHandler.sendPacket(UpdateLight(x, z, true))
            }
        }
        packetHandler.sendPacket(WorldBorder())

        return Behaviors.same()
    }

}

inline fun <reified T : BaseSystem> ArtemisWorld.getSystem(): T {
    return getSystem(T::class.java)
}
