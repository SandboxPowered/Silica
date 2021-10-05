package org.sandboxpowered.silica.client.server

import akka.actor.typed.ActorRef
import org.sandboxpowered.silica.server.Network
import org.sandboxpowered.silica.server.ServerProperties
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.vanilla.StateMappingManager
import org.sandboxpowered.silica.vanilla.VanillaProtocolMapping
import org.sandboxpowered.silica.world.SilicaWorld

class IntegratedServer() : SilicaServer() {
    override val stateRemapper = StateMappingManager()
    override val registryProtocolMapper = VanillaProtocolMapping()
    override val properties = IntegratedServerProperties()

    override lateinit var world: ActorRef<SilicaWorld.Command>
    override lateinit var network: ActorRef<Network>

    class IntegratedServerProperties : ServerProperties {
        override val onlineMode = true
        override val motd: String = "Singleplayer"
        override val serverPort: Int = 0
        override val serverIp: String = ""
        override val maxTickTime: Int = 60000
        override val maxPlayers: Int = 20
    }
}