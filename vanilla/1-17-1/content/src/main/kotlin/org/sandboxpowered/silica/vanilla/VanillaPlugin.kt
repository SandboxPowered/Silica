package org.sandboxpowered.silica.vanilla

import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin

@Plugin("minecraft", "1.17.1")
class VanillaPlugin : BasePlugin {
    override fun onEnable() {
        println("VanillaPlugin enabled!")
        SilicaInit.init()
    }

    override fun onDisable() {
        println("VanillaPlugin disabled!")
    }
}