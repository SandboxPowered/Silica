package org.sandboxpowered.silica.api.world

import akka.actor.typed.ActorRef
import org.sandboxpowered.silica.api.server.PlayerManager

interface World : WorldReader, WorldWriter {
    val playerManager: PlayerManager

    interface Command {
        class Ask<T>(val replyTo: ActorRef<T>, val body: (WorldReader) -> T) : Command

        open class DelayedCommand<F : World, R : Any>(val body: (F) -> R) : Command {
            class Perform(body: (World) -> Unit) : DelayedCommand<World, Unit>(body)
            class Ask<T : Any>(val replyTo: ActorRef<T>, body: (World) -> T) : DelayedCommand<World, T>(body)
        }
    }
}