package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import org.sandboxpowered.silica.StateManager
import org.sandboxpowered.silica.command.Commands
import org.sandboxpowered.silica.resources.ClasspathResourceLoader
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ResourceType
import org.sandboxpowered.silica.world.SilicaWorld
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.util.*

abstract class SilicaServer {
    var keyPair: KeyPair? = null
    val verificationArray = ByteArray(4)
    private val serverRandom = Random()
    val commands: Commands
    open var properties: ServerProperties? = null
    var dataManager: ResourceManager
    abstract val stateManager: StateManager
    abstract val world: ActorRef<SilicaWorld.Command>
    abstract val network: ActorRef<Network>

    init {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(1024)
            keyPair = keyPairGenerator.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
        serverRandom.nextBytes(verificationArray)
        commands = Commands()
        dataManager = ResourceManager(ResourceType.DATA)
        dataManager.add(ClasspathResourceLoader())
    }
}