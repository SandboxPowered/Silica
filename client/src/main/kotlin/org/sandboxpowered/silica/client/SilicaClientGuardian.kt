package org.sandboxpowered.silica.client

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.javadsl.*
import org.sandboxpowered.silica.client.SilicaClient.Command
import org.sandboxpowered.silica.client.server.IntegratedServer
import org.sandboxpowered.silica.util.Side
import org.sandboxpowered.silica.util.extensions.messageAdapter
import org.sandboxpowered.silica.util.extensions.onMessage
import org.sandboxpowered.silica.world.SilicaWorld
import java.time.Duration

class SilicaClientGuardian(
    val client: SilicaClient,
    context: ActorContext<Command>,
    timerScheduler: TimerScheduler<Command>,
    worldInit: (ActorRef<SilicaWorld.Command>) -> Unit
) : AbstractBehavior<Command>(context) {
    companion object {
        fun create(
            client: SilicaClient,
            worldInit: (ActorRef<SilicaWorld.Command>) -> Unit
        ): Behavior<Command> {
            return Behaviors.withTimers { timer ->
                Behaviors.setup {
                    SilicaClientGuardian(client, it, timer, worldInit)
                }
            }
        }
    }

    private val server: IntegratedServer = IntegratedServer()
    private val world: ActorRef<in SilicaWorld.Command> =
        context.spawn(SilicaWorld.actor(Side.CLIENT, server), "world").apply(worldInit).apply { server.world = this }

    init {
        context.watch(world)
        timerScheduler.startTimerWithFixedDelay("serverTick", Command.Tick(50f), Duration.ofMillis(50))
    }

    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onMessage(this::handleTick)
        .build()

    private fun handleTick(tick: Command.Tick): Behavior<Command> {
        world.tell(SilicaWorld.Command.Tick(tick.delta, context.messageAdapter { Command.Tock(it.done) }))
        return Behaviors.same()
    }
}