package org.sandboxpowered.silica.testplugin

import net.kyori.adventure.text.Component
import org.sandboxpowered.silica.api.ecs.component.PlayerInventoryComponent
import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.entity.EntityEvents
import org.sandboxpowered.silica.api.event.EventResult
import org.sandboxpowered.silica.api.event.TypedEventResult
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.server.ServerEvents
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.extensions.create
import org.sandboxpowered.silica.api.util.extensions.getComponent
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.world.World

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
        ServerEvents.CHAT_EVENT.subscribe { _, channel, message, _ ->
            TypedEventResult(EventResult.ALLOW, Component.text("[$channel] ").append(message))
        }
        ServerEvents.CHAT_COMMAND_EVENT.subscribe { _, _, message, world ->
            //TODO: Create a proper command system
            val parts = message.split(' ')
            if (parts.size >= 5 && parts[0] == "spawn") {
                val entity = Registries.ENTITY_DEFINITIONS[Identifier(parts[1])].orNull()
                if (entity != null) world.tell(World.Command.DelayedCommand.Perform {
                    it.spawnEntity(entity) { edit ->
                        val pos = edit.create<PositionComponent>().pos
                        pos.set(parts[2].toDouble() + .5, parts[3].toDouble(), parts[4].toDouble() + .5)
                    }
                })
            }
            TypedEventResult(EventResult.ALLOW, message)
        }
    }

    override fun onDisable() {
        logger.info("Test Plugin disabled!")
    }
}