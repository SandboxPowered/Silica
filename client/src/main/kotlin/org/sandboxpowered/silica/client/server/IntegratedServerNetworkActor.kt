package org.sandboxpowered.silica.client.server

import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.silica.server.Network
import org.sandboxpowered.silica.server.SilicaServer

class IntegratedServerNetworkActor(
    private val server: SilicaServer,
    context: ActorContext<Network>
) : AbstractBehavior<Network>(context) {
    override fun createReceive(): Receive<Network> = newReceiveBuilder().build()
}