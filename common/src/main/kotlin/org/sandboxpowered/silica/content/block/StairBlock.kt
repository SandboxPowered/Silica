package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.HALF
import org.sandboxpowered.silica.content.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.content.block.BlockProperties.STAIR_SHAPE
import org.sandboxpowered.silica.content.block.BlockProperties.WATERLOGGED
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.content.Direction
import org.sandboxpowered.silica.util.content.Half
import org.sandboxpowered.silica.util.content.StairShape
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

class StairBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun createDefaultState(baseState: BlockState): BlockState = baseState.modify {
        HALF set Half.BOTTOM
        HORIZONTAL_FACING set Direction.NORTH
        WATERLOGGED set false
        STAIR_SHAPE set StairShape.STRAIGHT
    }

    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, HORIZONTAL_FACING, HALF, STAIR_SHAPE)
    }

}