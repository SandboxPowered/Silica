package org.sandboxpowered.silica.client.server

import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.server.VanillaNetwork

class IntegratedServerNetworkActor(
    private val server: SilicaServer,
    context: ActorContext<VanillaNetwork>
) : AbstractBehavior<VanillaNetwork>(context) {
    override fun createReceive(): Receive<VanillaNetwork> = newReceiveBuilder().build()
}