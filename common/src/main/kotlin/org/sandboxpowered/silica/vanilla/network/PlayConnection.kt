package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.mojang.authlib.GameProfile
import org.sandboxpowered.silica.ecs.component.PlayerInventoryComponent
import org.sandboxpowered.silica.ecs.component.VanillaPlayerInput
import org.sandboxpowered.silica.ecs.system.SilicaPlayerManager
import org.sandboxpowered.silica.nbt.NBTCompound
import org.sandboxpowered.silica.nbt.nbt
import org.sandboxpowered.silica.nbt.setTag
import org.sandboxpowered.silica.server.Network
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.extensions.getSystem
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.vanilla.network.play.clientbound.*
import org.sandboxpowered.silica.world.SilicaWorld
import org.sandboxpowered.silica.world.VanillaWorldAdapter
import org.sandboxpowered.silica.world.util.BlocTree
import java.time.Duration
import kotlin.system.measureTimeMillis

sealed class PlayConnection {
    class ReceivePacket(val packet: PacketPlay) : PlayConnection()
    class SendPacket(val packet: PacketPlay) : PlayConnection()
    class ReceiveWorld(val blocks: BlocTree, val vanillaWorldAdapter: VanillaWorldAdapter) : PlayConnection()
    class ReceivePlayer(
        val gameProfiles: Array<GameProfile>,
        val input: VanillaPlayerInput,
        val inventoryComponent: PlayerInventoryComponent
    ) : PlayConnection()

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
    private lateinit var playerInventoryComponent: PlayerInventoryComponent

    private val playContext by lazy {
        PlayContext(
            server,
            { server.world.tell(SilicaWorld.Command.DelayedCommand.PerformSilica { _ -> it(playerInventoryComponent.inventory) }) },
            { server.world.tell(SilicaWorld.Command.DelayedCommand.Perform { _ -> it(playerInput) }) },
            { server.world.tell(SilicaWorld.Command.DelayedCommand.Perform { world -> it(world) }) })
    }

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
                ) { input, inventory, profiles ->
                    PlayConnection.ReceivePlayer(profiles, input, inventory)
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
        this.playerInventoryComponent = receive.inventoryComponent
        val overworld = Identifier.of("minecraft", "overworld")
        val overworldType: NBTCompound
        val codec = nbt {
            setTag("minecraft:dimension_type") {
                setString("type", "minecraft:dimension_type")

                val overworldTypeEntry = nbt {
                    setString("name", overworld.toString())
                    setInt("id", 0)
                    overworldType = nbt {
                        setBoolean("piglin_safe", false)
                        setBoolean("natural", true)
                        setFloat("ambient_light", 1f)
                        setString("infiniburn", "")
                        setBoolean("respawn_anchor_works", false)
                        setBoolean("has_skylight", true)
                        setBoolean("bed_works", true)
                        setString("effects", "minecraft:overworld")
                        setBoolean("has_raids", true)
                        setInt("min_y", 0)
                        setInt("height", 512)
                        setInt("logical_height", 256)
                        setFloat("coordinate_scale", 1f)
                        setBoolean("ultrawarm", false)
                        setBoolean("has_ceiling", false)
                    }
                    setTag("element", overworldType)
                }

                setList("value", listOf(overworldTypeEntry))
            }

            setTag("minecraft:worldgen/biome") {
                setString("type", "minecraft:worldgen/biome")

                val plainsBiomeEntry = nbt {
                    setString("name", "minecraft:plains")
                    setInt("id", 0)
                    setTag("element") {
                        setString("precipitation", "rain")
                        setFloat("depth", 0f)
                        setFloat("temperature", 0f)
                        setFloat("scale", 1f)
                        setFloat("downfall", 1f)
                        setString("category", "plains")
                        setTag("effects") {
                            setInt("sky_color", 8364543)
                            setInt("water_fog_color", 8364543)
                            setInt("fog_color", 8364543)
                            setInt("water_color", 8364543)
                        }
                    }
                }

                setList("value", listOf(plainsBiomeEntry))
            }
        }
        packetHandler.sendPacket(
            JoinGame(
                0,
                false,
                1,
                -1,
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
        packetHandler.sendPacket(HeldItemChangeClientbound(playerInventoryComponent.inventory.selectedSlot.toByte()))
        packetHandler.sendPacket(DeclareRecipes())
        packetHandler.sendPacket(DeclareTags())
        packetHandler.sendPacket(EntityStatus())
        packetHandler.sendPacket(DeclareCommands())
        packetHandler.sendPacket(UnlockRecipes())
        val currentPos = receive.input.wantedPosition
        packetHandler.sendPacket(
            SetPlayerPositionAndLook(
                currentPos.x,
                currentPos.y,
                currentPos.z,
                0f,
                0f,
                0.toByte(),
                0
            )
        )
        val gamemodes = IntArray(receive.gameProfiles.size)
        val pings = IntArray(receive.gameProfiles.size)
        receive.gameProfiles.forEachIndexed { index, uuid ->
            gamemodes[index] = 1
            pings[index] = 1
        }
        server.network.tell(
            Network.SendToAll(
                PlayerInfo.addPlayer(
                    receive.gameProfiles, gamemodes, pings
                )
            )
        )
        server.network.tell(
            Network.SendToAllExcept(
                receive.input.gameProfile.id,
                SpawnPlayer(
                    receive.input.playerId,
                    receive.input.gameProfile.id,
                    currentPos.x,
                    currentPos.y,
                    currentPos.z,
                    0, 0
                )
            )
        )
        server.world.tell(SilicaWorld.Command.DelayedCommand.PerformSilica {
            val system = it.artemisWorld.getSystem<SilicaPlayerManager>()
            system.onlinePlayers.forEach { t ->
                if (t != playerInput.gameProfile.id) {
                    val input = system.getVanillaInput(system.getPlayerId(t))
                    server.network.tell(
                        Network.SendToAllExcept(
                            input.gameProfile.id,
                            SpawnPlayer(
                                input.playerId,
                                input.gameProfile.id,
                                input.wantedPosition.x,
                                input.wantedPosition.y,
                                input.wantedPosition.z,
                                0, 0
                            )
                        )
                    )
                }
            }
        })
        packetHandler.sendPacket(UpdateChunkPosition(0, 0))

        server.world.tell(
            SilicaWorld.Command.Ask(
                { PlayConnection.ReceiveWorld((it as SilicaWorld).getTerrain(), it.vanillaWorldAdapter) },
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
        for (x in -4..4) {
            for (z in -4..4) {
                val time = measureTimeMillis {
                    packetHandler.sendPacket(ChunkData(x, z, world.blocks, world.vanillaWorldAdapter))
                }
//                logger.debug("Took {}ms to create Chunk Data for {} {}", time, x, z)
                packetHandler.sendPacket(UpdateLight(x, z, true))
            }
        }
        packetHandler.sendPacket(WorldBorder())
        packetHandler.sendPacket(
            InitWindowItems(
                0u,
                1 and 32767,
                playerInventoryComponent.inventory,
                server.registryProtocolMapper
            )
        )

        return Behaviors.same()
    }

}
