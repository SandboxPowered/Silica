package org.sandboxpowered.silica.client.server

import akka.actor.typed.ActorRef
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.server.ServerProperties
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.server.SilicaServer

class IntegratedServer() : SilicaServer() {
    override val properties = IntegratedServerProperties()

    override lateinit var world: ActorRef<World.Command>
    override lateinit var network: ActorRef<NetworkAdapter.Command>

    class IntegratedServerProperties : ServerProperties {
        override val onlineMode = true
        override val motd: String = "Singleplayer"
        override val serverPort: Int = 0
        override val serverIp: String = ""
        override val maxTickTime: Int = 60000
        override val maxPlayers: Int = 20
        override val supportChatFormatting: Boolean = true
        override val velocityEnabled: Boolean = false
        override val velocityKey: String = ""
    }

    override fun shutdown() {
        TODO("Not yet implemented")
    }
}