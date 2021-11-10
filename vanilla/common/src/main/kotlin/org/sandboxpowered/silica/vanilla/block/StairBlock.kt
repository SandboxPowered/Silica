package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.HALF
import org.sandboxpowered.silica.vanilla.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.vanilla.block.BlockProperties.STAIR_SHAPE
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WATERLOGGED
import org.sandboxpowered.silica.vanilla.util.Half
import org.sandboxpowered.silica.vanilla.util.StairShape

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