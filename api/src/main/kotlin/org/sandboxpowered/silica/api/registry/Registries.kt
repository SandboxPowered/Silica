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

    fun blocks(domain: String = "minecraft"): RegistryDelegate<Block> =
        blockDelegates.computeIfAbsent(domain) { RegistryDelegate(BLOCKS, it) }

    fun blocks(domain: String = "minecraft", optional: Boolean): RegistryDelegate.NullableRegistryDelegate<Block> =
        blockDelegates.computeIfAbsent(domain) { RegistryDelegate(BLOCKS, it) }.optional

    fun items(domain: String = "minecraft"): RegistryDelegate<Item> =
        itemDelegates.computeIfAbsent(domain) { RegistryDelegate(ITEMS, it) }

    fun items(domain: String = "minecraft", optional: Boolean): RegistryDelegate.NullableRegistryDelegate<Item> =
        itemDelegates.computeIfAbsent(domain) { RegistryDelegate(ITEMS, it) }.optional
}