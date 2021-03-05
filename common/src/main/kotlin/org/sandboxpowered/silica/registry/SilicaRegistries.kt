package org.sandboxpowered.silica.registry

import org.sandboxpowered.api.block.Block
import org.sandboxpowered.api.fluid.Fluid
import org.sandboxpowered.api.item.Item
import org.sandboxpowered.api.util.Identity

object SilicaRegistries {
    @JvmField
    val BLOCK_REGISTRY = SilicaRegistry(Identity.of("minecraft", "block"), Block::class.java)

    @JvmField
    val ITEM_REGISTRY = SilicaRegistry(Identity.of("minecraft", "item"), Item::class.java)

    @JvmField
    val FLUID_REGISTRY = SilicaRegistry(Identity.of("minecraft", "fluid"), Fluid::class.java)
}