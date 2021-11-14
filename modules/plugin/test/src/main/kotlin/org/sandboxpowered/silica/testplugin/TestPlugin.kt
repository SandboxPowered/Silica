package org.sandboxpowered.silica.testplugin

import net.kyori.adventure.text.Component
import org.sandboxpowered.silica.api.ecs.component.PlayerInventoryComponent
import org.sandboxpowered.silica.api.entity.EntityEvents
import org.sandboxpowered.silica.api.event.EventResult
import org.sandboxpowered.silica.api.event.TypedEventResult
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.server.ServerEvents
import org.sandboxpowered.silica.api.util.extensions.getComponent
import org.sandboxpowered.silica.api.util.getLogger

@Plugin(id = "test", version = "1.0.0", requirements = ["minecraft:content"])
class TestPlugin : BasePlugin {
    private val logger = getLogger()

    private val OAK_FENCE_GATE by Registries.blocks()
    private val SPRUCE_FENCE_GATE by Registries.blocks()

    override fun onEnable() {
        logger.info("Test Plugin enabled!")
        EntityEvents.SPAWN_ENTITY_EVENT.subscribe {
            val inv = it.getComponent<PlayerInventoryComponent>()
            if (inv != null) {
                inv.inventory.hotbar[0] = ItemStack(OAK_FENCE_GATE.item)
                inv.inventory.hotbar[1] = ItemStack(SPRUCE_FENCE_GATE.item)
            }
        }
        ServerEvents.CHAT_EVENT.subscribe { profile, channel, message ->
            TypedEventResult(EventResult.ALLOW, Component.text("[$channel] ").append(message))
        }
    }

    override fun onDisable() {
        logger.info("Test Plugin disabled!")
    }
}