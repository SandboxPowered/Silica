package org.sandboxpowered.silica

import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.internal.InternalAPI
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.registry.SilicaRegistries
import kotlin.reflect.KClass

class SilicaInternalAPI : InternalAPI {
    override fun <T : RegistryEntry<T>> getRegistry(kclass: KClass<T>): Registry<T> {
        if (kclass == Block::class)
            return SilicaRegistries.BLOCK_REGISTRY as Registry<T>
        if (kclass == Item::class)
            return SilicaRegistries.ITEM_REGISTRY as Registry<T>
        error("Unknown registry type $kclass")
    }
}