package org.sandboxpowered.silica.client.server

import akka.actor.typed.javadsl.AbstractBehavior
import akka.actor.typed.javadsl.ActorContext
import akka.actor.typed.javadsl.Receive
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.server.SilicaServer

class IntegratedServerNetworkActor(
    private val server: SilicaServer,
    context: ActorContext<NetworkAdapter.Command>
) : AbstractBehavior<NetworkAdapter.Command>(context) {
    override fun createReceive(): Receive<NetworkAdapter.Command> = newReceiveBuilder().build()
}