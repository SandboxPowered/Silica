package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.block.Block
import org.sandboxpowered.silica.fluid.Fluid
import org.sandboxpowered.silica.item.Item
import org.sandboxpowered.silica.util.Identifier

object SilicaRegistries {
    @JvmField
    val BLOCK_REGISTRY: Registry<Block> = SilicaRegistry(Identifier.of("minecraft", "block"), Block::class.java)

    @JvmField
    val ITEM_REGISTRY: Registry<Item> = SilicaRegistry(Identifier.of("minecraft", "item"), Item::class.java)

    @JvmField
    val FLUID_REGISTRY: Registry<Fluid> = SilicaRegistry(Identifier.of("minecraft", "fluid"), Fluid::class.java)
}