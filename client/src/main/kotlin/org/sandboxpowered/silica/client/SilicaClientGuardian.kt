package org.sandboxpowered.silica.client

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.*
import it.unimi.dsi.fastutil.objects.Object2LongMap
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap
import org.sandboxpowered.silica.api.util.Side
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.client.SilicaClient.Command
import org.sandboxpowered.silica.client.mesh.MeshRouter
import org.sandboxpowered.silica.client.server.IntegratedServer
import org.sandboxpowered.silica.util.extensions.messageAdapter
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.util.extensions.onSignal
import org.sandboxpowered.silica.world.SilicaWorld
import java.time.Duration

class SilicaClientGuardian private constructor(
    val client: SilicaClient,
    context: ActorContext<Command>,
    timerScheduler: TimerScheduler<Command>,
    worldInit: (ActorRef<SilicaWorld.Command>) -> Unit,
    meshRouterInit: (ActorRef<MeshRouter.Command>) -> Unit
) : AbstractBehavior<Command>(context) {
    companion object {
        fun create(
            client: SilicaClient,
            worldInit: (ActorRef<SilicaWorld.Command>) -> Unit,
            meshRouterInit: (ActorRef<MeshRouter.Command>) -> Unit
        ): Behavior<Command> = Behaviors.withTimers { timer ->
            Behaviors.setup {
                SilicaClientGuardian(client, it, timer, worldInit, meshRouterInit)
            }
        }
    }

    private val logger = getLogger()
    private var skippedTicks = 0
    private var lastTickTime: Long = -1
    private val server: IntegratedServer = IntegratedServer()
    private val world: ActorRef<in SilicaWorld.Command> =
        context.spawn(SilicaWorld.actor(Side.CLIENT, server), "world").apply(worldInit).apply { server.world = this }
    private val meshRouter: ActorRef<in MeshRouter.Command> = context.spawn(MeshRouter.actor(client), "meshRouter")
        .apply(meshRouterInit)
    private val currentlyTicking: Object2LongMap<ActorRef<*>> = Object2LongOpenHashMap(3)

    init {
        context.watch(world)
        context.watch(meshRouter)
        timerScheduler.startTimerAtFixedRate("serverTick", Command.Tick(50f), Duration.ofMillis(50))
        client.guardianStarted()
    }

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(this::handleTick)
        .onMessage(this::handleTock)
        .onSignal(this::terminated)
        .build()

    private fun terminated(terminated: Terminated): Behavior<Command> {
        logger.warn("${terminated.ref.path()} terminated")
        return Behaviors.stopped()
    }

    private fun handleTock(tock: Command.Tock): Behavior<Command> {
        val startTime = currentlyTicking.removeLong(tock.done)
        if (startTime == 0L) logger.warn("Received tock for actor which shouldn't be ticking : ${tock.done}")

        return Behaviors.same()
    }

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
            lastTickTime = System.currentTimeMillis()
            currentlyTicking.put(world, System.nanoTime())
            world.tell(SilicaWorld.Command.Tick(tick.delta, context.messageAdapter { Command.Tock(it.done) }))
        }
        return Behaviors.same()
    }
}