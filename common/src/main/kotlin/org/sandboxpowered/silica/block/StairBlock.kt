package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.block.SilicaBlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.block.SilicaBlockProperties.STAIR_HALF
import org.sandboxpowered.silica.block.SilicaBlockProperties.STAIR_SHAPE
import org.sandboxpowered.silica.block.SilicaBlockProperties.WATERLOGGED
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class StairBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, HORIZONTAL_FACING, STAIR_HALF, STAIR_SHAPE)
    }

    override fun createDefaultState(baseState: BlockState): BlockState {
        return super.createDefaultState(baseState)
    }
}