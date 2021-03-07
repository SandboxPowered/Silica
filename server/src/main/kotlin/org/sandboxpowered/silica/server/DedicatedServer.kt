package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.*
import com.google.inject.Guice
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.timeout.ReadTimeoutHandler
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import mu.toKLogger
import org.apache.logging.log4j.LogManager
import org.sandboxpowered.api.util.Side
import org.sandboxpowered.silica.StateManager
import org.sandboxpowered.silica.inject.SilicaImplementationModule
import org.sandboxpowered.silica.loading.SandboxLoader
import org.sandboxpowered.silica.network.*
import org.sandboxpowered.silica.util.messageAdapter
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.util.onSignal
import org.sandboxpowered.silica.world.SilicaWorld
import java.nio.file.Paths
import java.time.Duration

class DedicatedServer : SilicaServer() {
    var log = LogManager.getLogger()
    private var loader: SandboxLoader? = null
    private val stateManager = StateManager()
    private val acceptVanillaConnections: Boolean
    private lateinit var world: ActorRef<SilicaWorld.Command>

    init {
        Guice.createInjector(SilicaImplementationModule())
        loader = SandboxLoader()
        loader!!.load()
        val errors = stateManager.load()
        acceptVanillaConnections = errors.size == 0
        if (acceptVanillaConnections) {
            log.info("Accepting vanilla connections")
        } else {
            log.info("Found modded BlockStates, rejecting vanilla connections")
            log.info("Errors:")
            errors.forEach {
                log.info("   $it")
            }
        }
    }

    fun run() {
        val system = ActorSystem.create(DedicatedServerGuardian.create(this, this::world::set), "dedicatedServerGuardian")
//        system.terminate()
    }

    override fun getWorld() = this.world

    sealed class Command {
        class Tick(val delta: Float) : Command()
        class Tock(val done: ActorRef<*>) : Command()
    }

    private class DedicatedServerGuardian private constructor(
        server: SilicaServer, // don't like this
        context: ActorContext<Command>,
        timerScheduler: TimerScheduler<Command>,
        worldInit: (ActorRef<SilicaWorld.Command>) -> Unit
    ) : AbstractBehavior<Command>(context) {
        companion object {
            fun create(server: SilicaServer, worldInit: (ActorRef<SilicaWorld.Command>) -> Unit): Behavior<Command> = Behaviors.withTimers { timerScheduler ->
                Behaviors.setup {
                    DedicatedServerGuardian(server, it, timerScheduler, worldInit)
                }
            }
        }

        private val logger = context.log.toKLogger()
        private var skippedTicks = 0
        private val world: ActorRef<in SilicaWorld.Command> = context.spawn(SilicaWorld.actor(Side.SERVER), "world").apply(worldInit)
        private val network: ActorRef<in NetworkActor.Command> = context.spawn(NetworkActor.actor(server), "network")
        private val currentlyTicking: Object2LongMap<ActorRef<*>> = Object2LongOpenHashMap(3)

        init {
            context.watch(world)
            // TODO: compare to startTimerAtFixedRate
            timerScheduler.startTimerWithFixedDelay("serverTick", Command.Tick(50f), Duration.ofMillis(50))

            // TODO: wait for everything to be ready
            network.tell(NetworkActor.Command.Start(context.system.ignoreRef()))
        }

        override fun createReceive(): Receive<Command> = newReceiveBuilder()
            .onMessage(this::handleTick)
            .onMessage(this::handleTock)
            .onSignal(this::terminated)
            .build()

        private fun handleTick(tick: Command.Tick): Behavior<Command> {
            if (currentlyTicking.isNotEmpty()) ++skippedTicks
            else {
                if (skippedTicks > 0) {
                    logger.warn { "Skipped $skippedTicks ticks !" }
                    skippedTicks = 0
                }

                @Suppress("ReplacePutWithAssignment") // boxing
                currentlyTicking.put(world, System.nanoTime())
                currentlyTicking.put(network, System.nanoTime())
                world.tell(SilicaWorld.Command.Tick(tick.delta, context.messageAdapter { Command.Tock(it.done) }))
                network.tell(NetworkActor.Command.Tick(tick.delta, context.messageAdapter { Command.Tock(it.done) }))
            }

            return Behaviors.same()
        }

        private fun handleTock(tock: Command.Tock): Behavior<Command> {
            val startTime = currentlyTicking.removeLong(tock.done)
            if (startTime == 0L) logger.warn { "Received tock for actor which shouldn't be ticking : ${tock.done}" }
            // TODO: profiling ?

            return Behaviors.same()
        }

        private fun terminated(terminated: Terminated): Behavior<Command> {
            logger.warn { "${terminated.ref.path()} terminated" }
            return Behaviors.stopped()
        }
    }

    override fun getStateManager() = stateManager
}