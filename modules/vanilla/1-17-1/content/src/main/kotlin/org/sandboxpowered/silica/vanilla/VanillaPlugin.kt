package org.sandboxpowered.silica.vanilla

import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.world.WorldEvents

@Plugin("minecraft:content", "1.17.1", native = true)
class VanillaPlugin : BasePlugin {
    private val logger = getLogger()

    override fun onEnable() {
        logger.info("Minecraft content adapter v1.17.1 enabled!")
        SilicaInit.init()
        WorldEvents.REPLACE_BLOCKS_EVENT.subscribe { pos, old, new, _ ->
            if (old.block != new.block) logger.info("Replaced block at $pos from ${old.block} to ${new.block}")
            else logger.info("Replaced state at $pos from $old to $new")
        }
    }

    override fun onDisable() {
        logger.info("Minecraft content adapter v1.17.1 disabled!")
    }
}