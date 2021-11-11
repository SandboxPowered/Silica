package org.sandboxpowered.silica

import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.internal.InternalAPI
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.registry.SilicaRegistries
import kotlin.reflect.KClass

class SilicaInternalAPI : InternalAPI {
    @Suppress("UNCHECKED_CAST")
    override fun <T : RegistryEntry<T>> getRegistry(kclass: KClass<T>) = when (kclass) {
        Block::class -> SilicaRegistries.BLOCK_REGISTRY as Registry<T>
        Item::class -> SilicaRegistries.ITEM_REGISTRY as Registry<T>
        else -> error("Unknown registry type $kclass")
    }
}