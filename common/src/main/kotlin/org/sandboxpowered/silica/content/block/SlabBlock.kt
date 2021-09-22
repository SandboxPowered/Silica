package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.SLAB_HALF
import org.sandboxpowered.silica.content.block.BlockProperties.WATERLOGGED
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class SlabBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, SLAB_HALF)
    }

}