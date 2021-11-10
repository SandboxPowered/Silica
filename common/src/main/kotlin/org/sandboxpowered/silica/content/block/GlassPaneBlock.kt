package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.content.block.BlockProperties.EAST
import org.sandboxpowered.silica.content.block.BlockProperties.NORTH
import org.sandboxpowered.silica.content.block.BlockProperties.SOUTH
import org.sandboxpowered.silica.content.block.BlockProperties.WATERLOGGED
import org.sandboxpowered.silica.content.block.BlockProperties.WEST
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState

class GlassPaneBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(EAST, NORTH, SOUTH, WEST, WATERLOGGED)
    }
}