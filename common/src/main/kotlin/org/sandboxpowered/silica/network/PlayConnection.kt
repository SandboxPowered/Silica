package org.sandboxpowered.silica.network

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.artemis.BaseSystem
import com.artemis.Component
import com.artemis.EntityEdit
import com.mojang.authlib.GameProfile
import org.sandboxpowered.api.util.Identity
import org.sandboxpowered.silica.SilicaPlayerManager
import org.sandboxpowered.silica.nbt.CompoundTag
import org.sandboxpowered.silica.network.play.clientbound.*
import org.sandboxpowered.silica.server.NetworkActor
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.world.SilicaWorld
import org.sandboxpowered.silica.world.util.BlocTree
import java.util.*
import kotlin.reflect.KClass
import com.artemis.World as ArtemisWorld

class PlayConnection private constructor(
    val server: SilicaServer,
    private val packetHandler: PacketHandler,
    context: ActorContext<Command>
) : AbstractBehavior<PlayConnection.Command>(context) {

    companion object {
        fun actor(server: SilicaServer, packetHandler: PacketHandler): Behavior<Command> = Behaviors.setup {
            PlayConnection(server, packetHandler, it)
        }
    }

    sealed class Command {
        class ReceivePacket(val packet: PacketPlay) : Command()
        class SendPacket(val packet: PacketPlay) : Command()
        class ReceiveWorld(val blocks: BlocTree) : Command()
        class ReceivePlayer(val gameProfiles: Array<GameProfile>, val entity: Int, val world: SilicaWorld) : Command()
        class Disconnected(val profile: GameProfile) : Command()

        object Login : Command()
    }

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(this::handleLoginStart)
        .onMessage(this::handleSend)
        .onMessage(this::handleReceive)
        .onMessage(this::handleReceivePlayer)
        .onMessage(this::handleReceiveWorld)
        .onMessage(this::handleDisconnected)
        .build()

    private val logger = context.log

    init {
        this.packetHandler.setPlayConnection(context.self)
    }

    private fun handleReceive(receive: Command.ReceivePacket): Behavior<Command> {
        receive.packet.handle(this.packetHandler, this)

        return Behaviors.same()
    }

    private fun handleSend(receive: Command.SendPacket): Behavior<Command> {
        packetHandler.sendPacket(receive.packet)

        return Behaviors.same()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleLoginStart(login: Command.Login): Behavior<Command> {
        server.world.tell(SilicaWorld.Command.AskSilica({

            val playerManager = it.artemisWorld.getSystem<SilicaPlayerManager>()

            val entity = playerManager.create(packetHandler.connection.profile)

            Command.ReceivePlayer(playerManager.getOnlinePlayerProfiles(), entity, it)
        }, context.self))
        return Behaviors.same()
    }

    private fun handleReceivePlayer(player: Command.ReceivePlayer): Behavior<Command> {
        val overworld = Identity.of("minecraft", "overworld")
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
        packetHandler.sendPacket(SetPlayerPositionAndLook(8.0, 8.0, 8.0, 0f, 0f, 0.toByte(), 0))
        val gamemodes = IntArray(player.gameProfiles.size)
        val pings = IntArray(player.gameProfiles.size)
        player.gameProfiles.forEachIndexed { index, uuid ->
            gamemodes[index] = 1
            pings[index] = 1
        }
        server.network.tell(NetworkActor.Command.SendToAll(PlayerInfo.addPlayer(
            player.gameProfiles,gamemodes,pings
        )))
        packetHandler.sendPacket(UpdateChunkPosition(0, 0))

        server.world.tell(
            SilicaWorld.Command.Ask(
                { Command.ReceiveWorld((it as SilicaWorld).getTerrain()) },
                context.self
            )
        )

        return Behaviors.same()
    }
    private fun handleDisconnected(disconnected: Command.Disconnected): Behavior<Command> {
        server.network.tell(NetworkActor.Command.Disconnected(disconnected.profile))
        server.world.tell(SilicaWorld.Command.PerformSilica {
            it.artemisWorld.getSystem<SilicaPlayerManager>().disconnect(disconnected.profile)
        })
        return Behaviors.same()
    }
    private fun handleReceiveWorld(world: Command.ReceiveWorld): Behavior<Command> {
        logger.info("Sending world")
        for (x in -2..2) {
            for (z in -2..2) {
                val time = System.currentTimeMillis()
                packetHandler.sendPacket(ChunkData(x, z, world.blocks, server.stateManager::toVanillaId))
                logger.info("Took {}ms to create Chunk Data for {} {}", System.currentTimeMillis()-time, x, z)
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

private fun <T : Component> EntityEdit.create(kClass: KClass<T>) : T {
    return create(kClass.java)
}
