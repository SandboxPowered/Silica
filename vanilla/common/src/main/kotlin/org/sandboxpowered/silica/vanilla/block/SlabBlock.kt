package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.SLAB_HALF
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WATERLOGGED

class SlabBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, SLAB_HALF)
    }

}