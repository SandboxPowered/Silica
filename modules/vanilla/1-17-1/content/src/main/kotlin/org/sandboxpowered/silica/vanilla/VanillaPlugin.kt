package org.sandboxpowered.silica.vanilla

import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.util.getLogger

@Plugin("minecraft", "1.17.1")
class VanillaPlugin : BasePlugin {
    private val logger = getLogger()

    override fun onEnable() {
        logger.info("Minecraft content adapter v1.17.1 enabled!")
        SilicaInit.init()
    }

    override fun onDisable() {
        logger.info("Minecraft content adapter v1.17.1 disabled!")
    }
}