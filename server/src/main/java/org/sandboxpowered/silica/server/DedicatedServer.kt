package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import akka.actor.typed.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.*
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
import org.sandboxpowered.api.util.Side
import org.sandboxpowered.silica.loading.SandboxLoader
import org.sandboxpowered.silica.network.*
import org.sandboxpowered.silica.util.messageAdapter
import org.sandboxpowered.silica.util.onMessage
import org.sandboxpowered.silica.util.onSignal
import org.sandboxpowered.silica.world.SilicaWorld
import java.nio.file.Paths
import java.time.Duration

class DedicatedServer : SilicaServer() {
    private var loader: SandboxLoader? = null

    init {
        loader = SandboxLoader()
        loader!!.load()
    }

    fun oldRun() {
        val properties = ServerProperties.fromFile(Paths.get("server.properties"))
        val bossGroup: EventLoopGroup = NioEventLoopGroup()
        val workerGroup: EventLoopGroup = NioEventLoopGroup()
        try {
            val bootstrap = ServerBootstrap()
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .childHandler(object : ChannelInitializer<SocketChannel>() {
                    override fun initChannel(ch: SocketChannel) {
                        ch.pipeline()
                            .addLast(ReadTimeoutHandler(30))
                            .addLast(LengthSplitter())
                            .addLast(PacketDecoder(Flow.SERVERBOUND))
                            .addLast(LengthPrepender())
                            .addLast(PacketEncoder(Flow.CLIENTBOUND))
                            .addLast(PacketHandler(Connection(this@DedicatedServer)))
                    }
                })
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
            val future = bootstrap.bind(properties.serverPort).sync()
            future.channel().closeFuture().sync()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } finally {
            workerGroup.shutdownGracefully()
            bossGroup.shutdownGracefully()
        }
    }

    fun runActors() {
        val system = ActorSystem.create(DedicatedServerGuardian.create(), "dedicatedServerGuardian")
        oldRun()
        system.terminate()
    }

    sealed class Command {
        class Tick(val delta: Float) : Command()
        class Tock(val done: ActorRef<*>) : Command()
    }

    private class DedicatedServerGuardian private constructor(context: ActorContext<Command>, timerScheduler: TimerScheduler<Command>) : AbstractBehavior<Command>(context) {
        companion object {
            fun create(): Behavior<Command> = Behaviors.withTimers { timerScheduler -> Behaviors.setup {
                DedicatedServerGuardian(it, timerScheduler)
            } }
        }

        private val logger = context.log.toKLogger()
        private var skippedTicks = 0
        private val world: ActorRef<SilicaWorld.Command> = context.spawn(SilicaWorld.actor(Side.SERVER), "world")
        private val currentlyTicking: Object2LongMap<ActorRef<*>> = Object2LongOpenHashMap(3)

        init {
            context.watch(world)
            // TODO: compare to startTimerAtFixedRate
            timerScheduler.startTimerWithFixedDelay("serverTick", Command.Tick(50f), Duration.ofMillis(50))
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
                world.tell(SilicaWorld.Command.Tick(tick.delta, context.messageAdapter { Command.Tock(it.done) }))
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
}