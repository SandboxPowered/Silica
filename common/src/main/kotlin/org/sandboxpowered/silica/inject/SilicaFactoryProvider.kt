package org.sandboxpowered.silica.inject

import com.google.inject.Singleton
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.sandboxpowered.api.block.Material
import org.sandboxpowered.api.block.Materials
import org.sandboxpowered.api.events.EventHandlerFactory
import org.sandboxpowered.api.inject.FactoryProvider
import org.sandboxpowered.api.shape.Shape
import org.sandboxpowered.api.util.Identity
import org.sandboxpowered.api.util.math.Position
import org.sandboxpowered.api.util.math.Vec3i
import org.sandboxpowered.eventhandler.EventHandler
import org.sandboxpowered.eventhandler.ResettableEventHandler
import org.sandboxpowered.silica.inject.factory.IdentityFactory
import org.sandboxpowered.silica.inject.factory.PositionFactory
import org.sandboxpowered.silica.inject.factory.Vec3iFactory

@Singleton
class SilicaFactoryProvider : FactoryProvider {
    private val factories = Object2ObjectArrayMap<Class<*>, Any>()

    override fun <T : Any?> get(factoryClass: Class<T>): T {
        return factories[factoryClass] as T?
            ?: throw FactoryProvider.FactoryNotFoundException("Type '$factoryClass' has no factory.")
    }

    private inline fun <reified T : Any> registerFactory(factory: T) {
        factories[T::class.java] = factory
    }

    init {
        registerFactory<Position.Factory>(PositionFactory())
        registerFactory<Vec3i.Factory>(Vec3iFactory())
        registerFactory<Identity.Factory>(IdentityFactory())
        registerFactory<Shape.Factory>(object : Shape.Factory {
            override fun emptyCube(): Shape? {
                return null
            }

            override fun fullCube(): Shape? {
                return emptyCube()
            }

            override fun createCuboid(
                minX: Double,
                minY: Double,
                minZ: Double,
                maxX: Double,
                maxY: Double,
                maxZ: Double
            ): Shape? {
                return emptyCube()
            }
        })
        registerFactory<Materials.Factory>(Materials.Factory {
            object : Material {
                override fun getPistonInteraction(): Material.PistonInteraction {
                    TODO("Not yet implemented")
                }

                override fun doesBlockMovement(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun isBurnable(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun isBreakByHand(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun isLiquid(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun doesBlockLight(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun isReplaceable(): Boolean {
                    TODO("Not yet implemented")
                }

                override fun isSolid(): Boolean {
                    TODO("Not yet implemented")
                }

            }
        })

        registerFactory<EventHandlerFactory>(object : EventHandlerFactory {
            override fun <X : Any?> create(): EventHandler<X> {
                return ResettableEventHandler()
            }
        })
    }
}