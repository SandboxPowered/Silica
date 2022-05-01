package org.sandboxpowered.silica.testplugin

import com.mojang.brigadier.arguments.IntegerArgumentType
import net.kyori.adventure.text.Component
import org.joml.Vector3d
import org.sandboxpowered.silica.api.SilicaAPI
import org.sandboxpowered.silica.api.command.sync.ArgumentTypes
import org.sandboxpowered.silica.api.command.sync.SingletonArgumentSerializer
import org.sandboxpowered.silica.api.ecs.component.PlayerInventoryComponent
import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.ecs.component.VelocityComponent
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.entity.EntityEvents
import org.sandboxpowered.silica.api.event.EventResult
import org.sandboxpowered.silica.api.event.TypedEventResult
import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.registry.Registries.BLOCKS
import org.sandboxpowered.silica.api.server.ServerEvents
import org.sandboxpowered.silica.api.util.extensions.create
import org.sandboxpowered.silica.api.util.extensions.getArgument
import org.sandboxpowered.silica.api.util.extensions.getComponent
import org.sandboxpowered.silica.api.util.extensions.literal
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World

@Plugin(id = "test", version = "1.0.0", requirements = ["minecraft:content"])
object TestPlugin : BasePlugin {
    private val logger = getLogger()

    private val OAK_FENCE_GATE by BLOCKS
    private val SPRUCE_FENCE_GATE by BLOCKS

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
        ArgumentTypes.register("block_pos", SingletonArgumentSerializer { PositionArgumentType() })
        ArgumentTypes.register("entity_definition", SingletonArgumentSerializer { EntityDefinitionArgumentType() })
        ArgumentTypes.register("vec3d", SingletonArgumentSerializer { Vec3dArgumentType() })
        SilicaAPI.registerCommands {
            literal("spawn") {
                argument("entity", EntityDefinitionArgumentType()) {
                    argument("pos", PositionArgumentType()) {
                        executes {
                            val entity = it.getArgument<EntityDefinition>("entity")
                            val pos = it.getArgument<Position>("pos")
                            world.tell(World.Command.DelayedCommand.Perform {
                                it.spawnEntity(entity) { edit ->
                                    val epos = edit.create<PositionComponent>().pos
                                    epos.set(pos.x + .5, pos.y.toDouble(), pos.z + .5)
                                    val evelo = edit.create<VelocityComponent>().velocity
                                    evelo.set(0.0, 0.0, 0.1)
                                    sendMessage(Component.text("Spawned ${entity.identifier} (id ${edit.entityId})"))
                                }
                            })
                            1
                        }
                    }
                }
            }
            literal("velo") {
                argument("entity", IntegerArgumentType.integer(1)) {
                    argument("velocity", Vec3dArgumentType()) {
                        executes {
                            val entityId = it.getArgument<Int>("entity")
                            val pos = it.getArgument<Vector3d>("velocity")
                            world.tell(World.Command.DelayedCommand.Perform { world ->
                                world.updateEntity(entityId) { entity ->
                                    val velo = entity.getComponent<VelocityComponent>()?.velocity
                                    velo?.set(pos)
                                    sendMessage(Component.text("Set entity $entity's velocity"))
                                }
                            })

                            1
                        }
                    }
                }
            }
            literal("save") {
                executes {
                    world.tell(World.Command.DelayedCommand.Perform(World::saveWorld))
                    1
                }
            }
        }
    }

    override fun onDisable() {
        logger.info("Test Plugin disabled!")
    }
}