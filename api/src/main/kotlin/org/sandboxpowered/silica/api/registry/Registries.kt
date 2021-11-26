package org.sandboxpowered.silica.api.registry

import org.sandboxpowered.silica.api.SilicaAPI
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.internal.getRegistry
import org.sandboxpowered.silica.api.item.Item

object Registries {
    val BLOCKS: Registry<Block> = SilicaAPI.getRegistry()
    val ITEMS: Registry<Item> = SilicaAPI.getRegistry()
    val ENTITY_DEFINITIONS: Registry<EntityDefinition> = SilicaAPI.getRegistry()

    private val blockDelegates = HashMap<String, RegistryDelegate<Block>>()
    private val itemDelegates = HashMap<String, RegistryDelegate<Item>>()

    @Deprecated("Use BLOCKS instead.", ReplaceWith("BLOCKS.delegate(domain)"))
    fun blocks(domain: String = "minecraft"): RegistryDelegate<Block> = BLOCKS.delegate(domain)

    @Deprecated("Use BLOCKS instead.", ReplaceWith("BLOCKS.delegate(domain, optional)"))
    fun blocks(domain: String = "minecraft", optional: Boolean): RegistryDelegate.NullableRegistryDelegate<Block> =
        BLOCKS.delegate(domain, optional)

    @Deprecated("Use ITEMS instead.", ReplaceWith("ITEMS.delegate(domain)"))
    fun items(domain: String = "minecraft"): RegistryDelegate<Item> = ITEMS.delegate(domain)

    @Deprecated("Use ITEMS instead.", ReplaceWith("ITEMS.delegate(domain, optional)"))
    fun items(domain: String = "minecraft", optional: Boolean): RegistryDelegate.NullableRegistryDelegate<Item> =
        ITEMS.delegate(domain, optional)
}