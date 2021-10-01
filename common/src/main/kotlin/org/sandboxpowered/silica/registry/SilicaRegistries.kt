package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.content.block.Block
import org.sandboxpowered.silica.content.block.BlockEntityProvider
import org.sandboxpowered.silica.content.fluid.Fluid
import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.util.Identifier.Companion.of as id

object SilicaRegistries {
    val BLOCK_REGISTRY = SilicaRegistry(id("minecraft", "block"), Block::class.java).apply {
        addListener {
            if (it is BlockEntityProvider)
                BLOCKS_WITH_ENTITY.add(it)
        }
    }

    val BLOCKS_WITH_ENTITY = ArrayList<BlockEntityProvider>()

    val ITEM_REGISTRY = SilicaRegistry(id("minecraft", "item"), Item::class.java)

    val FLUID_REGISTRY = SilicaRegistry(id("minecraft", "fluid"), Fluid::class.java)

    init {
        SilicaInit.init()
    }
}