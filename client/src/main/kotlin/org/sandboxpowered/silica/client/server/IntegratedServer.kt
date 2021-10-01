package org.sandboxpowered.silica.client.server

import akka.actor.typed.ActorRef
import org.sandboxpowered.silica.server.Network
import org.sandboxpowered.silica.server.ServerProperties
import org.sandboxpowered.silica.server.SilicaServer
import org.sandboxpowered.silica.vanilla.StateMappingManager
import org.sandboxpowered.silica.vanilla.VanillaProtocolMapping
import org.sandboxpowered.silica.world.SilicaWorld
import java.nio.file.Paths

class IntegratedServer(
    override val world: ActorRef<SilicaWorld.Command>,
    override val network: ActorRef<Network>
) : SilicaServer() {
    override val stateRemapper = StateMappingManager()
    override val registryProtocolMapper = VanillaProtocolMapping()
    override val properties = ServerProperties.fromFile(Paths.get("server.properties"))
}