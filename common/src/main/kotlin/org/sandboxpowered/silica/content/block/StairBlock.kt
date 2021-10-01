package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.HALF
import org.sandboxpowered.silica.content.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.content.block.BlockProperties.STAIR_SHAPE
import org.sandboxpowered.silica.content.block.BlockProperties.WATERLOGGED
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

class StairBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, HORIZONTAL_FACING, HALF, STAIR_SHAPE)
    }

}