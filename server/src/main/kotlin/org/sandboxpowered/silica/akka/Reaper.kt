package org.sandboxpowered.silica.akka

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import akka.actor.typed.Terminated
import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Behaviors
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.silica.api.util.extensions.onMessage
import org.sandboxpowered.silica.api.util.extensions.onSignal
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.server.SilicaServer

/**
 * The Reaper is an Actor that is in charge of collecting dead souls.
 * He has been told to watch over a number of Actors, waiting for them die.
 * When he sees the last one give up its ghost he performs some action, and
 * that action will be, in this case, to shut down the [SilicaServer].
 * Never fear the reaper.
 */
class Reaper private constructor(val server: SilicaServer, context: ActorContext<Command>) :
    AbstractBehavior<Reaper.Command>(context) {
    private val logger = getLogger()
    override fun createReceive(): Receive<Command> = newReceiveBuilder()
        .onSignal(this::terminated)
        .onMessage(this::onMessage)
        .build()

    private val watchingRefs = HashSet<ActorRef<*>>()

    private fun terminated(terminated: Terminated): Behavior<Command> {
        logger.warn("${terminated.ref.path()} terminated")
        watchingRefs -= terminated.ref
        return if (watchingRefs.isEmpty()) Behaviors.stopped(server::shutdown)
        else Behaviors.same()
    }

    private fun onMessage(cmd: Command.MarkForReaping): Behavior<Command> {
        logger.info("${cmd.ref.path()} marked for reaping")
        watchingRefs += cmd.ref
        context.watch(cmd.ref)
        return Behaviors.same()
    }

    sealed class Command {
        class MarkForReaping(val ref: ActorRef<*>) : Command()
    }

    companion object {
        fun actor(server: SilicaServer): Behavior<Command> = Behaviors.setup {
            Reaper(server, it)
        }
    }
}