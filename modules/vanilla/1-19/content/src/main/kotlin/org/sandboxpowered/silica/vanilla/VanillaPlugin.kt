package org.sandboxpowered.silica.vanilla

import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin

@Plugin("minecraft", "1.19")
class VanillaPlugin : BasePlugin {
    override fun onEnable() {
        error("1.19 Unsupported")
    }

    override fun onDisable() {

    }
}