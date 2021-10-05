package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import com.google.gson.GsonBuilder
import net.kyori.adventure.text.Component
import org.sandboxpowered.silica.command.Commands
import org.sandboxpowered.silica.resources.ClasspathResourceLoader
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ResourceType
import org.sandboxpowered.silica.util.extensions.registerTypeAdapter
import org.sandboxpowered.silica.vanilla.StateMappingManager
import org.sandboxpowered.silica.vanilla.VanillaProtocolMapping
import org.sandboxpowered.silica.world.SilicaWorld
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.util.*

abstract class SilicaServer {
    companion object {
        val gson = GsonBuilder()
            .registerTypeAdapter(MOTDDeserializer())
            .registerTypeAdapter(MOTDSerializer())
            .create()
    }

    var keyPair: KeyPair? = null
    val verificationArray = ByteArray(4)
    private val serverRandom = Random()
    val commands: Commands
    abstract val properties: ServerProperties
    var dataManager: ResourceManager
    abstract val stateRemapper: StateMappingManager
    abstract val registryProtocolMapper: VanillaProtocolMapping
    abstract val world: ActorRef<SilicaWorld.Command>
    abstract val network: ActorRef<Network>
    val motd = MOTD(Version("Sandbox Silica", -1), Players(0, 0, ArrayList()), Component.empty(), "")

    var motdCache: String = gson.toJson(motd)
        private set

    fun updateMOTDCache() {
        motdCache = gson.toJson(motd)
    }

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
        dataManager.add(ClasspathResourceLoader("Silica", arrayOf("silica")))
    }
}