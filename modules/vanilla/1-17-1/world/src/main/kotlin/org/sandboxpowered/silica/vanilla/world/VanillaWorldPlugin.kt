package org.sandboxpowered.silica.vanilla.world

import org.sandboxpowered.silica.api.SilicaAPI
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.util.getLogger

@Plugin(
    id = "minecraft:world",
    version = "1.17.1",
    requirements = ["minecraft:content@1.17.1"],
    after = ["minecraft:content"],
    before = ["minecraft:network"],
    native = true
)
class VanillaWorldPlugin : BasePlugin {
    private val logger = getLogger()
    override fun onEnable() {
        logger.info("Minecraft world adapter v1.17.1 enabled")
        SilicaAPI.registerWorldGenerator(VanillaWorldGenerator)
    }

    override fun onDisable() {
        logger.info("Minecraft world adapter v1.17.1 disabled!")
    }
}