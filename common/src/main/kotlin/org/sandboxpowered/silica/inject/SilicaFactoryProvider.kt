package org.sandboxpowered.silica.inject

import com.google.inject.Singleton
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap
import org.sandboxpowered.api.engine.FactoryProvider
import org.sandboxpowered.api.util.Identifier
import org.sandboxpowered.silica.inject.factory.IdentityFactory

@Singleton
class SilicaFactoryProvider : FactoryProvider {
    private val factories = Object2ObjectArrayMap<Class<*>, Any>()

    override fun <T : Any?> provide(factoryClass: Class<T>): T {
        return factories[factoryClass] as T?
            ?: throw RuntimeException("Type '$factoryClass' has no factory.")
    }

    private inline fun <reified T : Any> registerFactory(factory: T) {
        factories[T::class.java] = factory
    }

    init {
//        registerFactory<Position.PositionFactory>(PositionFactory())
//        registerFactory<Vec3i.Factory>(Vec3iFactory())
        registerFactory<Identifier.IdentifierFactory>(IdentityFactory())

    }
}