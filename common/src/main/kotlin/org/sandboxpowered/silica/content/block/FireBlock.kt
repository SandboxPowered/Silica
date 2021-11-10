package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.content.block.BlockProperties.AGE_15
import org.sandboxpowered.silica.content.block.BlockProperties.EAST
import org.sandboxpowered.silica.content.block.BlockProperties.NORTH
import org.sandboxpowered.silica.content.block.BlockProperties.SOUTH
import org.sandboxpowered.silica.content.block.BlockProperties.UP
import org.sandboxpowered.silica.content.block.BlockProperties.WEST
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState

class FireBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(AGE_15, UP, NORTH, EAST, SOUTH, WEST)
    }
}