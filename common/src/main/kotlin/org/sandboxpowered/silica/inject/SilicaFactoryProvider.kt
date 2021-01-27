package org.sandboxpowered.silica.inject

import com.google.inject.Singleton
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.sandboxpowered.api.events.EventHandlerFactory
import org.sandboxpowered.api.inject.FactoryProvider
import org.sandboxpowered.api.util.math.Position
import org.sandboxpowered.eventhandler.EventHandler
import org.sandboxpowered.eventhandler.ResettableEventHandler
import org.sandboxpowered.silica.inject.factory.PositionFactory

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

        registerFactory<EventHandlerFactory>(object : EventHandlerFactory {
            override fun <X : Any?> create(): EventHandler<X> {
                return ResettableEventHandler()
            }
        })
    }
}