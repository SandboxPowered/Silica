package org.sandboxpowered.silica.server

import com.mojang.authlib.minecraft.MinecraftSessionService
import com.mojang.authlib.yggdrasil.YggdrasilAuthenticationService
import org.sandboxpowered.silica.api.server.Server
import org.sandboxpowered.silica.command.Commands
import org.sandboxpowered.silica.resources.ClasspathResourceLoader
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ResourceType
import java.net.Proxy
import java.util.*

abstract class SilicaServer : Server {
    private val serverRandom = Random()
    val commands: Commands = Commands()
    var dataManager: ResourceManager = ResourceManager(ResourceType.DATA)
    override val sessionService: MinecraftSessionService =
        YggdrasilAuthenticationService(Proxy.NO_PROXY).createMinecraftSessionService()

    abstract fun shutdown()

    init {
        dataManager.add(ClasspathResourceLoader("Silica", arrayOf("silica", "minecraft")))
    }
}