package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.UP
import org.sandboxpowered.silica.content.block.BlockProperties.WALL_SHAPE_EAST
import org.sandboxpowered.silica.content.block.BlockProperties.WALL_SHAPE_NORTH
import org.sandboxpowered.silica.content.block.BlockProperties.WALL_SHAPE_SOUTH
import org.sandboxpowered.silica.content.block.BlockProperties.WALL_SHAPE_WEST
import org.sandboxpowered.silica.content.block.BlockProperties.WATERLOGGED
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class WallBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, WALL_SHAPE_EAST, WALL_SHAPE_NORTH, WALL_SHAPE_WEST, WALL_SHAPE_SOUTH, UP)
    }

}