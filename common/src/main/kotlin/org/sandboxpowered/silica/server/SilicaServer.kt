package org.sandboxpowered.silica.server

import akka.actor.typed.ActorRef
import com.mojang.authlib.minecraft.MinecraftSessionService
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import org.sandboxpowered.silica.command.Commands
import org.sandboxpowered.silica.resources.ClasspathResourceLoader
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ResourceType
import org.sandboxpowered.silica.vanilla.StateMappingManager
import org.sandboxpowered.silica.vanilla.VanillaProtocolMapping
import org.sandboxpowered.silica.world.SilicaWorld
import java.net.Proxy
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.util.*

abstract class SilicaServer {

    var keyPair: KeyPair = run {
        try {
            val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
            keyPairGenerator.initialize(1024)
            keyPairGenerator.generateKeyPair()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
    val verificationArray = ByteArray(4)
    private val serverRandom = Random()
    val commands: Commands
    abstract val properties: ServerProperties
    var dataManager: ResourceManager
    abstract val stateRemapper: StateMappingManager
    abstract val registryProtocolMapper: VanillaProtocolMapping
    abstract val world: ActorRef<SilicaWorld.Command>
    abstract val vanillaNetwork: ActorRef<VanillaNetwork>
    val sessionService: MinecraftSessionService = YggdrasilAuthenticationService(Proxy.NO_PROXY).createMinecraftSessionService()

    abstract fun shutdown()

    init {
        serverRandom.nextBytes(verificationArray)
        commands = Commands()
        dataManager = ResourceManager(ResourceType.DATA)
        dataManager.add(ClasspathResourceLoader("Silica", arrayOf("silica")))
    }
}