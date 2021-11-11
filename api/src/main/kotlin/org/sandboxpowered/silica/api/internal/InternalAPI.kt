package org.sandboxpowered.silica.api.internal

import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.world.generation.WorldGenerator
import java.util.*
import kotlin.reflect.KClass

fun getInstance(): InternalAPI {
    val first = ServiceLoader.load(InternalAPI::class.java).findFirst()
    require(first.isPresent) { "Unable to find Internal API instance." }
    return first.get()
}

interface InternalAPI {
    fun <T : RegistryEntry<T>> getRegistry(kclass: KClass<T>): Registry<T>
    fun registerNetworkAdapter(adapter: NetworkAdapter)
    fun registerWorldGenerator(generator: WorldGenerator)
}

inline fun <reified T : RegistryEntry<T>> InternalAPI.getRegistry(): Registry<T> = getRegistry(T::class)
