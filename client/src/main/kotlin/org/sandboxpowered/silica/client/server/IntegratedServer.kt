package org.sandboxpowered.silica.client.server

import akka.actor.typed.ActorRef
import org.sandboxpowered.silica.StateManager
import org.sandboxpowered.silica.server.Network
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.world.SilicaWorld

class IntegratedServer : SilicaServer() {
    override fun getStateManager(): StateManager {
        TODO("Not yet implemented")
    }

    override fun getWorld(): ActorRef<SilicaWorld.Command> {
        TODO("Not yet implemented")
    }

    override fun getNetwork(): ActorRef<Network> {
        TODO("Not yet implemented")
    }
}