package org.sandboxpowered.silica

import org.apache.logging.log4j.LogManager
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.internal.InternalAPI
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.plugin.PluginManager
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.world.generation.WorldGenerator
import org.sandboxpowered.silica.registry.SilicaRegistries
import kotlin.reflect.KClass

class SilicaInternalAPI : InternalAPI {
    private val log = LogManager.getLogger("SilicaAPI")

    override val pluginManager: PluginManager
        get() = TODO("Not yet implemented")

    override fun registerNetworkAdapter(adapter: NetworkAdapter) {
        log.info("Registered network adapter ${adapter.id} for protocol ${adapter.protocol}")
        // TODO("Not yet implemented")
    }

    override fun registerWorldGenerator(generator: WorldGenerator) {
        log.info("Registered world generator ${generator.id}")
        // TODO("Not yet implemented")
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : RegistryEntry<T>> getRegistry(kclass: KClass<T>) = when (kclass) {
        Block::class -> SilicaRegistries.BLOCK_REGISTRY as Registry<T>
        Item::class -> SilicaRegistries.ITEM_REGISTRY as Registry<T>
        else -> error("Unknown registry type $kclass")
    }
}