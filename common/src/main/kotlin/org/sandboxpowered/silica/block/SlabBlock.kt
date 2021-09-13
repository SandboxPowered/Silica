package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.block.SilicaBlockProperties.SLAB_HALF
import org.sandboxpowered.silica.block.SilicaBlockProperties.WATERLOGGED
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class SlabBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, SLAB_HALF)
    }

}