package org.sandboxpowered.silica.api.network

import akka.actor.typed.ActorRef
import akka.actor.typed.Behavior
import org.sandboxpowered.silica.api.server.Server
import org.sandboxpowered.silica.api.util.Identifier

interface NetworkAdapter {
    val id: Identifier
    val protocol: Identifier

    fun createBehavior(server: Server): Behavior<Command>

    interface Command {
        class Tick(val delta: Float, val replyTo: ActorRef<Tock>) : Command {
            class Tock(val done: ActorRef<Command>)
        }

        class Start(val replyTo: ActorRef<in Boolean>) : Command
    }
}