package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.*
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import net.kyori.adventure.text.minimessage.MiniMessage
import org.apache.commons.io.FileUtils
import org.sandboxpowered.silica.resources.ZIPResourceLoader
import org.sandboxpowered.silica.util.Side
import org.sandboxpowered.silica.util.Util
import org.sandboxpowered.silica.util.Util.MINECRAFT_VERSION
import org.sandboxpowered.silica.util.Util.getLogger
import org.sandboxpowered.silica.util.extensions.join
import org.sandboxpowered.silica.util.extensions.messageAdapter
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.util.extensions.onSignal
import org.sandboxpowered.silica.vanilla.StateMappingManager
import org.sandboxpowered.silica.vanilla.StateMappingManager.ErrorType.UNKNOWN
import org.sandboxpowered.silica.vanilla.VanillaProtocolMapping
import org.sandboxpowered.silica.world.SilicaWorld
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Paths
import java.time.Duration

class DedicatedServer(args: Args) : SilicaServer() {
    private var logger = getLogger<DedicatedServer>()
    override val stateRemapper = StateMappingManager()
    override val registryProtocolMapper = VanillaProtocolMapping()
    private val acceptVanillaConnections: Boolean
    override lateinit var world: ActorRef<SilicaWorld.Command>
    override lateinit var network: ActorRef<Network>
    private val stateRemappingErrors: Map<StateMappingManager.ErrorType, Set<String>>
    override val properties = DedicatedServerProperties.fromFile(Paths.get("server.properties"))

    class Args()

    init {
        val mcArchive = Util.ensureMinecraftVersion(MINECRAFT_VERSION, Side.SERVER)
        dataManager.add(ZIPResourceLoader("Minecraft $MINECRAFT_VERSION", mcArchive))

        motd.version.name = "1.17.1 - Vanilla"
        motd.version.protocol = 756

        motd.description = MiniMessage.markdown().parse(properties.motd)
        motd.favicon =
            "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAEAAAABACAYAAACqaXHeAAACxklEQVR4nO2av24TQRDGv7NP8Z8oSiQcR5cGB11JQ0EKKHgARKQg0SAeAQlegJI+Lc8QCaQg6CidmhpFSkhnJ0hRXES2Yplqw3nji+92Zm4Oeb/uzr692d/N7cy3dhBF0QQLrIp2ANryALQD0JYHoB2AtjwA7QC05QFoB6AtD0A7AG2FRd6sG8dO1z09OmKO5J8CSTPUDkN86XTYx+UEIgLgoNPBvVA+uThAsK8B3TguZPIA8H5lhTwGK4APa2ucwxUi1kc1GI+x3+vdHL/a2OAcXkSiuZqEAfACscd2VaFlkBvI3mBAuh4QqAJPajVsLy3lvu7F+jrqlWxLkgHJAYCcAaa5MSXpcDjE4XB483nWlfrr2dnU8bzs+HV9nSfMVJEzIK27S6vRrqUrCWS/12N5+gARwF4U4fHy8tzv/R6N8Pr09Nb5EMBbRyClAPBpcxMPm83c11Gzg2vyAHEN+HFx4QTAfm0MEHtiHJ3ePBVaBtOUBwjn0wdKAsBWEsjO8TH+jMcAeFPfiASAqxTdpYOtralj7r0Bkhn6ORqxtaRZ1Y1j542VWWJxg0VDANx3l2yRG6FZK/XzVgvNapUybGZRXwnyIng1maARBFPnvp2fTx2X2RazmKG89ZobCCUL2NxgAOCdQ+Nyv17H9uoq6d6lAGCLw/RkVSkAmJ1gSReYplIA0LDF8+6RReIAkto9OUF/Rve402jggcNWuoGhDoD7J6+82UHxCKpmqAy2WGxLjKrvl5f42O/fOt+qVPDG2oWiZEBpAdi663VZCAC2uGwxyQ1qTZ7TfZIAaNhg7nuSq4AJ6GW7jarlCjklBZutDH62VmwOx1dEhon1AZLBc26OkqvAs1oNjxx+DHWRxK4wqx2W6tgkJm4k+i8xChDJSSclCuB/0ML/U9QD0A5AWx6AdgDa8gC0A9CWB6AdgLY8AO0AtPUXrV7219gkQOMAAAAASUVORK5CYII="
        motd.players.max = properties.maxPlayers

        updateMOTDCache()

        stateRemappingErrors = stateRemapper.load()
        registryProtocolMapper.load()
        acceptVanillaConnections = stateRemappingErrors.isEmpty()
    }

    fun run() {
        if (acceptVanillaConnections) {
            logger.info("Accepting vanilla connections")
        } else {
            val unknown = stateRemappingErrors[UNKNOWN]
            if (unknown != null && unknown.isNotEmpty()) {
                logger.warn("Found ${unknown.size} custom BlockStates")
                unknown.forEach {
                    logger.warn("   $it")
                }
            }
            val missing = stateRemappingErrors[StateMappingManager.ErrorType.MISSING]
            if (missing != null && missing.isNotEmpty()) {
                logger.error("Missing ${missing.size} vanilla BlockStates. Exported to missing.txt")
                val builder = StringBuilder()
                missing.sorted().forEach {
                    builder.append(it).append("\n")
                }
                FileUtils.writeStringToFile(File("missing.txt"), builder.toString(), StandardCharsets.UTF_8)
            }
            logger.error("Rejecting vanilla connections")
        }
        logger.info("Loaded namespaces: [${dataManager.getNamespaces().join(",")}]")
        val system = ActorSystem.create(
            DedicatedServerGuardian.create(this, this::world::set, this::network::set),
            "dedicatedServerGuardian"
        )
//        system.terminate()
    }

    sealed class Command {
        class Tick(val delta: Float) : Command()
        class Tock(val done: ActorRef<*>) : Command()
    }

    private class DedicatedServerGuardian private constructor(
        val server: SilicaServer, // don't like this
        context: ActorContext<Command>,
        timerScheduler: TimerScheduler<Command>,
        worldInit: (ActorRef<SilicaWorld.Command>) -> Unit,
        networkInit: (ActorRef<Network>) -> Unit
    ) : AbstractBehavior<Command>(context) {
        companion object {
            fun create(
                server: SilicaServer,
                worldInit: (ActorRef<SilicaWorld.Command>) -> Unit,
                networkInit: (ActorRef<Network>) -> Unit
            ): Behavior<Command> = Behaviors.withTimers { timerScheduler ->
                Behaviors.setup {
                    DedicatedServerGuardian(server, it, timerScheduler, worldInit, networkInit)
                }
            }
        }

        private val logger = getLogger<DedicatedServerGuardian>()
        private var skippedTicks = 0
        private var lastTickTime: Long = -1
        private val world: ActorRef<in SilicaWorld.Command> =
            context.spawn(SilicaWorld.actor(Side.SERVER, server), "world").apply(worldInit)
        private val network: ActorRef<in Network> = context.spawn(Network.actor(server), "network").apply(networkInit)
        private val currentlyTicking: Object2LongMap<ActorRef<*>> = Object2LongOpenHashMap(3)

        init {
            context.watch(world)
            context.watch(network)
            // TODO: compare to startTimerAtFixedRate
            timerScheduler.startTimerWithFixedDelay("serverTick", Command.Tick(50f), Duration.ofMillis(50))

            // TODO: wait for everything to be ready
            network.tell(Network.Start(context.system.ignoreRef()))
        }

        override fun createReceive(): Receive<Command> = newReceiveBuilder()
            .onMessage(this::handleTick)
            .onMessage(this::handleTock)
            .onSignal(this::terminated)
            .build()

        private fun handleTick(tick: Command.Tick): Behavior<Command> {
            if (currentlyTicking.isNotEmpty()) {
                val lastTickOffset = System.currentTimeMillis() - lastTickTime
                if (server.properties.maxTickTime != -1 && lastTickOffset >= server.properties.maxTickTime) {
                    TODO("Terminate server after taking too long")
                }
                ++skippedTicks
            } else {
                if (skippedTicks > 0) {
                    val lastTickOffset = System.currentTimeMillis() - lastTickTime
                    logger.warn("Skipped $skippedTicks ticks! took ${lastTickOffset}ms")
                    skippedTicks = 0
                }

                @Suppress("ReplacePutWithAssignment") // boxing
                currentlyTicking.put(world, System.nanoTime())
                @Suppress("ReplacePutWithAssignment") // boxing
                currentlyTicking.put(network, System.nanoTime())
                lastTickTime = System.currentTimeMillis()
                world.tell(SilicaWorld.Command.Tick(tick.delta, context.messageAdapter { Command.Tock(it.done) }))
                network.tell(Network.Tick(tick.delta, context.messageAdapter { Command.Tock(it.done) }))
            }

            return Behaviors.same()
        }

        private fun handleTock(tock: Command.Tock): Behavior<Command> {
            val startTime = currentlyTicking.removeLong(tock.done)
            if (startTime == 0L) logger.warn("Received tock for actor which shouldn't be ticking : ${tock.done}")
            // TODO: profiling ?

            return Behaviors.same()
        }

        private fun terminated(terminated: Terminated): Behavior<Command> {
            logger.warn("${terminated.ref.path()} terminated")
            return Behaviors.stopped()
        }
    }
}