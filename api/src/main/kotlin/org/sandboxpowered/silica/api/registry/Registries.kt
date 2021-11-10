package org.sandboxpowered.silica.api.registry

import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.internal.SilicaAPI
import org.sandboxpowered.silica.api.item.Item

object Registries {
    val BLOCKS = SilicaAPI.getRegistry<Block>()
    val ITEMS = SilicaAPI.getRegistry<Item>()

    private val blockDelegates = HashMap<String, RegistryDelegate<Block>>()
    private val itemDelegates = HashMap<String, RegistryDelegate<Item>>()

    fun blocks(domain: String = "minecraft"): RegistryDelegate<Block> =
        blockDelegates.computeIfAbsent(domain) { RegistryDelegate(BLOCKS, it) }

    fun blocks(domain: String = "minecraft", required: Boolean): RegistryDelegate.NonNullRegistryDelegate<Block> =
        blockDelegates.computeIfAbsent(domain) { RegistryDelegate(BLOCKS, it) }.guaranteed

    fun items(domain: String = "minecraft"): RegistryDelegate<Item> =
        itemDelegates.computeIfAbsent(domain) { RegistryDelegate(ITEMS, it) }

    fun items(domain: String = "minecraft", required: Boolean): RegistryDelegate.NonNullRegistryDelegate<Item> =
        itemDelegates.computeIfAbsent(domain) { RegistryDelegate(ITEMS, it) }.guaranteed
}