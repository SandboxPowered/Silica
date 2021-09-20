package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.block.SilicaBlockProperties.AGE_15
import org.sandboxpowered.silica.block.SilicaBlockProperties.EAST
import org.sandboxpowered.silica.block.SilicaBlockProperties.NORTH
import org.sandboxpowered.silica.block.SilicaBlockProperties.SOUTH
import org.sandboxpowered.silica.block.SilicaBlockProperties.UP
import org.sandboxpowered.silica.block.SilicaBlockProperties.WEST
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class FireBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(AGE_15, UP, NORTH, EAST, SOUTH, WEST)
    }
}