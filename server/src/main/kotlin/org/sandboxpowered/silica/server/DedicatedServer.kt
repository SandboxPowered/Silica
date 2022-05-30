package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.*
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import org.reflections.Reflections
import org.sandboxpowered.silica.SilicaInternalAPI
import org.sandboxpowered.silica.akka.Reaper
import org.sandboxpowered.silica.api.internal.InternalAPI
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.util.Side
import org.sandboxpowered.silica.api.util.extensions.WithContext
import org.sandboxpowered.silica.api.util.extensions.onMessage
import org.sandboxpowered.silica.api.util.extensions.onSignal
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.data.recipe.RecipeManager
import org.sandboxpowered.silica.resources.ZIPResourceLoader
import org.sandboxpowered.silica.util.VanillaLocator
import org.sandboxpowered.silica.util.VanillaLocator.MINECRAFT_VERSION
import org.sandboxpowered.silica.util.extensions.getAnnotation
import org.sandboxpowered.silica.util.extensions.getTypesAnnotatedWith
import org.sandboxpowered.silica.world.SilicaWorld
import java.nio.file.Paths
import java.time.Duration
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set
import kotlin.system.exitProcess

class DedicatedServer(args: Args) : SilicaServer() {
    private var logger = getLogger()
    override lateinit var world: ActorRef<World.Command>
    override lateinit var network: ActorRef<NetworkAdapter.Command>
    override val properties = DedicatedServerProperties.fromFile(Paths.get("server.properties"))
    private lateinit var system: ActorSystem<Command>

    class Args

    init {
        val mcArchive = VanillaLocator.ensureMinecraftVersion(MINECRAFT_VERSION, Side.SERVER)
        dataManager.add(ZIPResourceLoader("Minecraft $MINECRAFT_VERSION", mcArchive))
    }

    override fun shutdown() {
        system.terminate()
        exitProcess(0)
    }

    fun run() {
        if (!properties.onlineMode) logger.warn("Server running in Offline Mode! This will be unsupported in Silica 1.0")
        logger.info("Loaded namespaces: [${dataManager.getNamespaces().joinToString()}]")
        //TODO: make this use an plugin classloader rather than package search
        val classes = Reflections("org.sandboxpowered").getTypesAnnotatedWith<Plugin>()
        val log = getLogger()
        log.info("Loading ${classes.size} plugins")
        val map = sortedMapOf<Plugin, BasePlugin>(compareBy<Plugin> { !it.native }.thenComparing { o1, o2 ->
            if (o1.id in o2.before) 1 else if (o2.id in o1.before) -1 else 0
        }.thenComparing { o1, o2 ->
            if (o1.id in o2.after) -1 else if (o2.id in o1.after) 1 else 0
        })
        //TODO: Make dependencies load in the correct order
        classes.forEach {
            val plugin = it.getAnnotation<Plugin>()
            if (BasePlugin::class.java.isAssignableFrom(it)) {
                val instance = (it.kotlin.objectInstance ?: it.getConstructor().newInstance()) as BasePlugin
                map[plugin] = instance
            }
        }
        map.forEach { (plugin, instance) ->
            log.debug("Loading ${plugin.id}@${plugin.version}")
            instance.onEnable()
        }

        RecipeManager().load(dataManager)

        system = ActorSystem.create(
            DedicatedServerGuardian.create(this, this::world::set, this::network::set),
            "dedicatedServerGuardian"
        )
    }

    sealed interface Command {
        class Tick(val delta: Float) : Command
        class Tock(val done: ActorRef<*>) : Command
        class Failed(val failed: ActorRef<*>, val throwable: Throwable?) : Command
    }

    private class DedicatedServerGuardian private constructor(
        val server: SilicaServer, // don't like this
        context: ActorContext<Command>,
        timerScheduler: TimerScheduler<Command>,
        worldInit: (ActorRef<World.Command>) -> Unit,
        networkInit: (ActorRef<NetworkAdapter.Command>) -> Unit
    ) : AbstractBehavior<Command>(context), WithContext<Command> {
        companion object {
            fun create(
                server: SilicaServer,
                worldInit: (ActorRef<World.Command>) -> Unit,
                networkInit: (ActorRef<NetworkAdapter.Command>) -> Unit
            ): Behavior<Command> = Behaviors.withTimers { timerScheduler ->
                Behaviors.setup {
                    DedicatedServerGuardian(server, it, timerScheduler, worldInit, networkInit)
                }
            }
        }

        private var skippedTicks = 0
        private var lastTickTime: Long = -1
        private val world: ActorRef<in World.Command> =
            context.spawn(SilicaWorld.actor(Side.SERVER, server), "world").apply(worldInit)
        private val networkAdapter: ActorRef<in NetworkAdapter.Command> = run {
            val api = InternalAPI.instance as SilicaInternalAPI
            val adapter = api.networkAdapter ?: error("No network adapter found")
            context.spawn(adapter.createBehavior(server), "network").apply(networkInit)
        }
        private val reaper: ActorRef<in Reaper.Command> = context.spawn(Reaper.actor(server), "reaper")
        private val currentlyTicking: Object2LongMap<ActorRef<*>> = Object2LongOpenHashMap(3)

        init {
            context.watch(world)
            context.watch(networkAdapter)
            // TODO: compare to startTimerAtFixedRate
            timerScheduler.startTimerAtFixedRate("serverTick", Command.Tick(50f), Duration.ofMillis(50))

            reaper.tell(Reaper.Command.MarkForReaping(world))
            reaper.tell(Reaper.Command.MarkForReaping(networkAdapter))

            // TODO: wait for everything to be ready
            networkAdapter.tell(NetworkAdapter.Command.Start(context.system.ignoreRef()))
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
                    logger.error("Single tick took >=${server.properties.maxTickTime}ms! took ${lastTickOffset}ms")
                    context.stop(world)
                    context.stop(networkAdapter)
                    server.shutdown()
                    return Behaviors.stopped()
                }
                ++skippedTicks
            } else {
                if (skippedTicks > 0) {
                    val lastTickOffset = System.currentTimeMillis() - lastTickTime
                    logger.warn("Skipped $skippedTicks ticks! took ${lastTickOffset}ms")
                    skippedTicks = 0
                }

                currentlyTicking[world] = System.nanoTime()
                currentlyTicking[networkAdapter] = System.nanoTime()
                lastTickTime = System.currentTimeMillis()
                world.ask { it: ActorRef<SilicaWorld.Command.Tick.Tock> ->
                    SilicaWorld.Command.Tick(tick.delta, it)
                }.pipeToSelf { tock, throwable ->
                    if (tock != null) Command.Tock(tock.done)
                    else Command.Failed(world, throwable)
                }

                networkAdapter.ask { it: ActorRef<NetworkAdapter.Command.Tick.Tock> ->
                    NetworkAdapter.Command.Tick(tick.delta, it)
                }.pipeToSelf { tock, throwable ->
                    if (tock != null) Command.Tock(tock.done)
                    else Command.Failed(world, throwable)
                }
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