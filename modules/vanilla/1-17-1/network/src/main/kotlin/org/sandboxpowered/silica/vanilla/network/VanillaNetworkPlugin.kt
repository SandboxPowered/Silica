package org.sandboxpowered.silica.vanilla.network

import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin

@Plugin(id = "minecraft-network", version = "1.17.1", requirements = ["minecraft:1.17.1"])
class VanillaNetworkPlugin : BasePlugin {
    override fun onEnable() {
        println("Minecraft network adapter v1.17.1 enabled!")
    }

    override fun onDisable() {
        println("Minecraft network adapter v1.17.1 disabled!")
    }
}