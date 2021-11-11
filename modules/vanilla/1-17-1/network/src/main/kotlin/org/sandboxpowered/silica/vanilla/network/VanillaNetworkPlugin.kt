package org.sandboxpowered.silica.vanilla.network

import org.sandboxpowered.silica.api.network.NetworkManager
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.util.getLogger

@Plugin(id = "minecraft-network", version = "1.17.1", requirements = ["minecraft:1.17.1"])
class VanillaNetworkPlugin : BasePlugin {
    private val logger = getLogger()
    override fun onEnable() {
        logger.info("Minecraft network adapter v1.17.1 enabled!")
        NetworkManager.registerNetworkAdapter(VanillaNetworkAdapter())
    }

    override fun onDisable() {
        logger.info("Minecraft network adapter v1.17.1 disabled!")
    }
}