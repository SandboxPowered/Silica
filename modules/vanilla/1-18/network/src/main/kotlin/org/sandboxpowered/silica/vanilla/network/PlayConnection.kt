package org.sandboxpowered.silica.vanilla.network

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import com.mojang.authlib.GameProfile
import org.joml.Vector3d
import org.sandboxpowered.silica.api.SilicaAPI
import org.sandboxpowered.silica.api.ecs.component.PlayerInventoryComponent
import org.sandboxpowered.silica.api.internal.getRegistry
import org.sandboxpowered.silica.api.nbt.NBTCompound
import org.sandboxpowered.silica.api.nbt.nbt
import org.sandboxpowered.silica.api.nbt.setTag
import org.sandboxpowered.silica.api.network.Packet
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.server.Server
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.extensions.*
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.vanilla.network.ecs.component.VanillaPlayerInputComponent
import org.sandboxpowered.silica.vanilla.network.packets.HandledPacket
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.packets.play.clientbound.*
import org.sandboxpowered.silica.vanilla.network.packets.play.clientbound.world.VanillaChunkSection
import org.sandboxpowered.silica.vanilla.network.util.mapping.VanillaProtocolMapping
import java.time.Duration

sealed interface PlayConnection {
    class ReceivePacket(val packet: Packet) : PlayConnection
    class SendPacket(val packet: Packet) : PlayConnection
    object ReceiveWorld : PlayConnection
    class ReceiveChunkSections(val x: Int, val z: Int, val chunkSections: Array<out VanillaChunkSection>) :
        PlayConnection

    class ReceivePlayer(
        val gameProfiles: Array<GameProfile>,
        val input: VanillaPlayerInputComponent,
        val inventoryComponent: PlayerInventoryComponent
    ) : PlayConnection

    class Disconnected(val profile: GameProfile) : PlayConnection

    class FailedPlayerCreation(val reason: String) : PlayConnection

    object Login : PlayConnection

    companion object {
        fun actor(
            server: Server,
            packetHandler: PacketHandler,
            vanillaWorldAdapter: ActorRef<in VanillaWorldAdapter>
        ): Behavior<PlayConnection> = Behaviors.setup {
            PlayConnectionActor(server, packetHandler, vanillaWorldAdapter, it)
        }
    }
}

private class PlayConnectionActor(
    private val server: Server,
    private val packetHandler: PacketHandler,
    private val vanillaWorldAdapter: ActorRef<in VanillaWorldAdapter>,
    context: ActorContext<PlayConnection>
) : AbstractBehavior<PlayConnection>(context), WithContext<PlayConnection> {

    override fun createReceive(): Receive<PlayConnection> = newReceiveBuilder()
        .onMessage(this::handleLoginStart)
        .onMessage(this::handleSend)
        .onMessage(this::handleReceive)
        .onMessage(this::handleReceivePlayer)
        .onMessage(this::handleFailedToCreatePlayer)
        .onMessage(this::handleReceiveChunkSection)
        .onMessage(this::handleReceiveWorld)
        .onMessage(this::handleDisconnected)
        .build()

    // TODO: apply at the right time + unsafe to keep a ref to a component
    private lateinit var playerInput: VanillaPlayerInputComponent
    private lateinit var playerInventoryComponent: PlayerInventoryComponent

    private val playContext by lazy {
        val itemMapper = VanillaProtocolMapping.INSTANCE["minecraft:item"]
        PlayContext(
            { Registries.ITEMS[itemMapper[it]] },
            { itemMapper[it.identifier] },
            { server.world.tell(World.Command.DelayedCommand.Perform { _ -> it(playerInventoryComponent.inventory) }) },
            { server.world.tell(World.Command.DelayedCommand.Perform { _ -> it(playerInput) }) },
            server.world,
            server.network,
            server.properties
        )
    }

    init {
        this.packetHandler.setPlayConnection(context.self)
    }

    private fun handleReceive(receive: PlayConnection.ReceivePacket): Behavior<PlayConnection> {
        if (receive.packet is PacketPlay) receive.packet.handle(this.packetHandler, this.playContext)
        else if (receive.packet is HandledPacket) receive.packet.handle(this.packetHandler, packetHandler.connection)

        return Behaviors.same()
    }

    private fun handleSend(send: PlayConnection.SendPacket): Behavior<PlayConnection> {
        packetHandler.sendPacket(send.packet)

        return Behaviors.same()
    }

    @Suppress("UNUSED_PARAMETER")
    private fun handleLoginStart(login: PlayConnection.Login): Behavior<PlayConnection> {
        context.ask(
            PlayConnection.ReceivePlayer::class.java, server.world, Duration.ofSeconds(1),
            {
                World.Command.DelayedCommand.Ask(it) { world ->
                    val manager = world.playerManager
                    val player = manager.createPlayer(packetHandler.connection.profile)
                    val input =
                        player.getComponent<VanillaPlayerInputComponent>() ?: error("Player has no input component")
                    val inventory =
                        player.getComponent<PlayerInventoryComponent>() ?: error("Player has no inventory component")
                    val onlinePlayers = manager.onlinePlayerProfiles
                    PlayConnection.ReceivePlayer(onlinePlayers, input, inventory)
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
        val overworld = Identifier("minecraft", "overworld")
        val overworldType: NBTCompound
        val codec = nbt {
            "minecraft:dimension_type" to nbt {
                "type" to "minecraft:dimension_type"

                val overworldTypeEntry = nbt {
                    "name" to overworld.toString()
                    "id" to 0
                    overworldType = nbt {
                        "piglin_safe" to false
                        "natural" to true
                        "ambient_light" to 1f
                        "infiniburn" to "#minecraft:infiniburn_overworld"
                        "respawn_anchor_works" to false
                        "has_skylight" to true
                        "bed_works" to true
                        "effects" to "minecraft:overworld"
                        "has_raids" to true
                        "min_y" to 0
                        "height" to 512
                        "logical_height" to 256
                        "coordinate_scale" to 1.0
                        "ultrawarm" to false
                        "has_ceiling" to false
                    }
                    "element" to overworldType
                }

                "value" to listOf(overworldTypeEntry)
            }

            setTag("minecraft:worldgen/biome") {
                "type" to "minecraft:worldgen/biome"

                val plainsBiomeEntry = nbt {
                    "name" to "minecraft:plains"
                    "id" to 0
                    setTag("element") {
                        "precipitation" to "rain"
                        "depth" to 0f
                        "temperature" to 0f
                        "scale" to 1f
                        "downfall" to 1f
                        "category" to "plains"
                        setTag("effects") {
                            "sky_color" to 8364543
                            "water_fog_color" to 8364543
                            "fog_color" to 8364543
                            "water_color" to 8364543
                        }
                    }
                }

                "value" to listOf(plainsBiomeEntry)
            }
        }
        packetHandler.sendPacket(
            S2CJoinGame(
                playerId = 0,
                hardcore = false,
                gamemode = 0,
                previousGamemode = -1,
                worldNames = listOf(overworld),
                dimCodec = codec,
                dim = overworldType,
                world = overworld,
                seed = 0,
                maxPlayers = 20,
                viewDistance = 4,
                simulationDistance = 4,
                reducedDebug = false,
                respawnScreen = true,
                debug = false,
                flat = true
            )
        )
        packetHandler.sendPacket(S2CPluginChannel(Identifier("minecraft", "brand")) {
            it.writeString("silica")
        })
        packetHandler.sendPacket(S2CHeldItemChange(playerInventoryComponent.inventory.selectedSlot.toByte()))
        packetHandler.sendPacket(S2CDeclareRecipes(SilicaAPI.getRegistry<Recipe>().values.values))
        packetHandler.sendPacket(S2CDeclareTags())
        packetHandler.sendPacket(S2CEntityStatus(0, 24))
        packetHandler.sendPacket(S2CDeclareCommands(SilicaAPI.commandDispatcher.root))
        packetHandler.sendPacket(S2CUnlockRecipes())
        val currentPos = receive.input.wantedPosition
        wantedPos = currentPos

        val gamemodes = IntArray(receive.gameProfiles.size)
        val pings = IntArray(receive.gameProfiles.size)
        receive.gameProfiles.forEachIndexed { index, uuid ->
            gamemodes[index] = 1
            pings[index] = 1
        }
        server.network.tell(
            VanillaNetworkAdapter.VanillaCommand.SendToAll(
                S2CPlayerInfo.addPlayer(
                    receive.gameProfiles, gamemodes, pings
                )
            )
        )
        server.network.tell(
            VanillaNetworkAdapter.VanillaCommand.SendToAllExcept(
                receive.input.gameProfile.id,
                S2CSpawnPlayer(
                    receive.input.playerId,
                    receive.input.gameProfile.id,
                    currentPos.x,
                    currentPos.y,
                    currentPos.z,
                    0, 0
                )
            )
        )
        server.world.tell(World.Command.DelayedCommand.Perform {
            val system = it.playerManager
            system.onlinePlayers.forEach { t ->
                if (t != playerInput.gameProfile.id) {
                    val input = system.getEntity(system.getPlayerId(t))?.getComponent<VanillaPlayerInputComponent>()
                        ?: error("Couldn't get input component for player $t")
                    server.network.tell(
                        VanillaNetworkAdapter.VanillaCommand.SendToAllExcept(
                            input.gameProfile.id,
                            S2CSpawnPlayer(
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
        packetHandler.sendPacket(S2CUpdateChunkPosition(0, 0))

        for (x in -4..4) {
            for (z in -4..4) {
                context.spawnAnonymous(
                    Accumulator.actor(16, vanillaWorldAdapter) { idx, ref: ActorRef<in VanillaChunkSection> ->
                        VanillaWorldAdapter.GetChunkSection(ChunkSectionPos(x, idx, z), ref)
                    }
                ).ask { actorRef: ActorRef<in Array<out VanillaChunkSection>> -> Accumulator.Start(actorRef) }
                    .thenAccept { context.self.tell(PlayConnection.ReceiveChunkSections(x, z, it)) }
                    .onException { context.log.warn("Couldn't receive chunk sections", it) }
            }
        }

        context.self.tell(PlayConnection.ReceiveWorld)

        return Behaviors.same()
    }

    private fun handleDisconnected(disconnected: PlayConnection.Disconnected): Behavior<PlayConnection> {
        server.network.tell(VanillaNetworkAdapter.VanillaCommand.Disconnected(disconnected.profile))
        server.world.tell(World.Command.DelayedCommand.Perform {
            it.playerManager.disconnect(disconnected.profile)
        })
        return Behaviors.stopped()
    }

    private fun handleFailedToCreatePlayer(failure: PlayConnection.FailedPlayerCreation): Behavior<PlayConnection> {
        // TODO: disconnect
        context.log.error("Could not create player: ${failure.reason}")
        return Behaviors.stopped()
    }

    private var wantedPos = Vector3d()
    private var sectionsCount = 0

    private fun handleReceiveChunkSection(sections: PlayConnection.ReceiveChunkSections): Behavior<PlayConnection> {
        packetHandler.sendPacket(S2CChunkData(sections.x, sections.z, sections.chunkSections))
        packetHandler.sendPacket(S2CUpdateLight(sections.x, sections.z, true, emptyList(), emptyList()))

        if (sectionsCount < 81 && ++sectionsCount == 81) {
            packetHandler.sendPacket(
                S2CSetPlayerPositionAndLook(wantedPos.x, wantedPos.y, wantedPos.z, 0f, 0f, 0.toByte(), 0)
            )
        }
        return Behaviors.same()
    }

    private fun handleReceiveWorld(message: PlayConnection.ReceiveWorld): Behavior<PlayConnection> {
        packetHandler.sendPacket(S2CWorldBorder())
        packetHandler.sendPacket(
            S2CInitWindowItems(
                0u,
                1 and 32767,
                playerInventoryComponent.inventory
            )
        )

        return Behaviors.same()
    }

}
