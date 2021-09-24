package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.content.block.Block
import org.sandboxpowered.silica.content.block.BlockEntityProvider
import org.sandboxpowered.silica.content.fluid.Fluid
import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.util.Identifier.Companion.of as id

object SilicaRegistries {
    @JvmField
    val BLOCK_REGISTRY = SilicaRegistry(id("minecraft", "block"), Block::class.java).apply {
        addListener {
            if (it is BlockEntityProvider)
                BLOCKS_WITH_BE.add(it)
        }
    }

    val BLOCKS_WITH_BE = ArrayList<BlockEntityProvider>()

    @JvmField
    val ITEM_REGISTRY = SilicaRegistry(id("minecraft", "item"), Item::class.java)

    @JvmField
    val FLUID_REGISTRY = SilicaRegistry(id("minecraft", "fluid"), Fluid::class.java)

    init {
        SilicaInit.init()
    }
}