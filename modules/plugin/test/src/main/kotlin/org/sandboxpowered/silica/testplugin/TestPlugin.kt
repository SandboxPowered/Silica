package org.sandboxpowered.silica.testplugin

import org.sandboxpowered.silica.api.ecs.component.PlayerInventoryComponent
import org.sandboxpowered.silica.api.entity.EntityEvents
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.extensions.getComponent
import org.sandboxpowered.silica.api.util.getLogger

@Plugin(id = "test", version = "1.0.0", requirements = ["minecraft:content"])
class TestPlugin : BasePlugin {
    private val logger = getLogger()

    val OAK_FENCE_GATE by Registries.blocks()
    val SPRUCE_FENCE_GATE by Registries.blocks()

    override fun onEnable() {
        logger.info("Test Plugin enabled!")
        EntityEvents.SPAWN_ENTITY_EVENT.subscribe {
            val inv = it.getComponent<PlayerInventoryComponent>()
            if(inv != null) {
                inv.inventory.hotbar[0] = ItemStack(OAK_FENCE_GATE.item)
                inv.inventory.hotbar[1] = ItemStack(SPRUCE_FENCE_GATE.item)
            }
        }
    }

    override fun onDisable() {
        logger.info("Test Plugin disabled!")
    }
}