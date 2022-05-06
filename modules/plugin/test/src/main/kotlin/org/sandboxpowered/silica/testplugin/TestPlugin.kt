package org.sandboxpowered.silica.testplugin

import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import net.kyori.adventure.text.Component
import org.joml.Vector3d
import org.joml.Vector3f
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
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registries.BLOCKS
import org.sandboxpowered.silica.api.server.ServerEvents
import org.sandboxpowered.silica.api.util.extensions.create
import org.sandboxpowered.silica.api.util.extensions.getArgument
import org.sandboxpowered.silica.api.util.extensions.getComponent
import org.sandboxpowered.silica.api.util.extensions.literal
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.utilities.Identifier
import kotlin.random.Random

@Plugin(id = "test", version = "1.0.0", requirements = ["minecraft:content"])
object TestPlugin : BasePlugin {
    private val logger = getLogger()

    private val OAK_FENCE_GATE by BLOCKS
    private val SPRUCE_FENCE_GATE by BLOCKS
    private val DIRT by BLOCKS

    override fun onEnable() {
        logger.info("Test Plugin enabled!")
        EntityEvents.SPAWN_ENTITY_EVENT.subscribe {
            val inv = it.getComponent<PlayerInventoryComponent>()
            if (inv != null) {
                inv.inventory.hotbar[0] = ItemStack(OAK_FENCE_GATE.item)
                inv.inventory.hotbar[1] = ItemStack(SPRUCE_FENCE_GATE.item)
                inv.inventory.hotbar[2] = ItemStack(DIRT.item, 64)
            }
        }
        ServerEvents.CHAT_EVENT.subscribe { _, channel, message, _ ->
            TypedEventResult(EventResult.ALLOW, Component.text("[$channel] ").append(message))
        }
        ArgumentTypes.register("block_pos", SingletonArgumentSerializer { PositionArgumentType() })
        ArgumentTypes.register("entity_definition", SingletonArgumentSerializer { EntityDefinitionArgumentType() })
        ArgumentTypes.register("vec3d", SingletonArgumentSerializer { Vec3dArgumentType() })
        ArgumentTypes.register("vec3f", SingletonArgumentSerializer { Vec3fArgumentType() })
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
                                    val velocityComponent = edit.create<VelocityComponent>()
                                    val direction = velocityComponent.direction
                                    direction.set(0.0, 0.0, 0.1)
                                    velocityComponent.velocity = .3f
                                    sendMessage(Component.text("Spawned ${entity.identifier} (id ${edit.entityId})"))
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
            literal("horde") {
                argument("amount", IntegerArgumentType.integer(1)) {
                    executes {
                        val amount = it.getArgument<Int>("amount")
                        val entityDef = Registries.ENTITY_DEFINITIONS[Identifier("minecraft:zombie")].get()
                        world.tell(World.Command.DelayedCommand.Perform { world ->
                            val rng = Random(Random.nextInt())
                            repeat(amount) {
                                world.spawnEntity(entityDef) { edit ->
                                    val epos = edit.create<PositionComponent>().pos
                                    epos.set(rng.nextInt(-5, 5) + .5, 7.1, rng.nextInt(-5, 5) + .5)
                                    val velocityComponent = edit.create<VelocityComponent>()
                                    val direction = velocityComponent.direction
                                    direction.set(rng.nextDouble(-.2, .2), 0.0, rng.nextDouble(-.2, .2))
                                    velocityComponent.velocity = rng.nextDouble(.01, .5).toFloat()
                                    sendMessage(Component.text("Spawned ${entityDef.identifier} (id ${edit.entityId})"))
                                }
                            }
                        })

                        1
                    }
                }
            }
            literal("tp") {
                argument("pos", Vec3dArgumentType()) {
                    executes {
                        val pos = it.getArgument<Vector3d>("pos")
                        world.tell(World.Command.DelayedCommand.Perform { world ->
                            world.updateEntity(1) { e ->
                                e.getComponent<PositionComponent>()?.pos?.set(pos)
                                sendMessage(Component.text("Teleported to $pos"))
                            }
                        })

                        1
                    }
                }
            }
            literal("velocity") {
                argument("entity", IntegerArgumentType.integer(0)) {
                    argument("direction", Vec3fArgumentType()) {
                        argument("speed", FloatArgumentType.floatArg()) {
                            executes {
                                val entity = it.getArgument<Int>("entity")
                                val direction = it.getArgument<Vector3f>("direction")
                                val speed = it.getArgument<Float>("speed")
                                world.tell(World.Command.DelayedCommand.Perform { world ->
                                    world.updateEntity(entity) { e ->
                                        e.getComponent<VelocityComponent>()?.apply {
                                            direction.set(direction.normalize())
                                            velocity = speed
                                        }
                                        sendMessage(Component.text("Updated entity $entity's velocity ($direction @$speed)"))
                                    }
                                })

                                1
                            }
                        }
                    }
                }
            }
            literal("kill") {
                argument("entity", IntegerArgumentType.integer(0)) {
                    executes {
                        val entity = it.getArgument<Int>("entity")
                        world.tell(World.Command.DelayedCommand.Perform { world ->
                            world.killEntity(entity)
                            sendMessage(Component.text("Killed $entity"))
                        })

                        1
                    }
                }
            }
        }
    }

    override fun onDisable() {
        logger.info("Test Plugin disabled!")
    }
}