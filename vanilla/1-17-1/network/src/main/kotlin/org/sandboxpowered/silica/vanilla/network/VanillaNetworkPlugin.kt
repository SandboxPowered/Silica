package org.sandboxpowered.silica.vanilla.network

import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin

@Plugin(id = "minecraft-network", version = "1.17.1", requirements = ["minecraft:1.17.1"])
class VanillaNetworkPlugin : BasePlugin {
    override fun onEnable() {
        println("VanillaNetworkPlugin enabled")
    }

    override fun onDisable() {
        println("VanillaNetworkPlugin disabled")
    }
}