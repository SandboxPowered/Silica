package org.sandboxpowered.silica.api.internal

import com.artemis.BaseEntitySystem
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.plugin.PluginManager
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.world.generation.WorldGenerator
import java.util.*
import kotlin.reflect.KClass


interface InternalAPI {
    val pluginManager: PluginManager
    fun <T : RegistryEntry<T>> getRegistry(kclass: KClass<T>): Registry<T>
    fun registerNetworkAdapter(adapter: NetworkAdapter)
    fun registerWorldGenerator(generator: WorldGenerator)
    fun registerSystem(system: BaseEntitySystem)
    fun registerListener(listener: Any)

    companion object {
        val instance: InternalAPI = run {
            val first = ServiceLoader.load(InternalAPI::class.java).findFirst()
            require(first.isPresent) { "Unable to find Internal API instance." }
            first.get()
        }
    }
}

inline fun <reified T : RegistryEntry<T>> InternalAPI.getRegistry(): Registry<T> = getRegistry(T::class)
