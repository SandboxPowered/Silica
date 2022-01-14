package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.utilities.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.UP
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WALL_SHAPE_EAST
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WALL_SHAPE_NORTH
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WALL_SHAPE_SOUTH
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WALL_SHAPE_WEST
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WATERLOGGED

class WallBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, WALL_SHAPE_EAST, WALL_SHAPE_NORTH, WALL_SHAPE_WEST, WALL_SHAPE_SOUTH, UP)
    }

}