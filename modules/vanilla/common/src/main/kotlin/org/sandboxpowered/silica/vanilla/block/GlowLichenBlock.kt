package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.DOWN
import org.sandboxpowered.silica.vanilla.block.BlockProperties.EAST
import org.sandboxpowered.silica.vanilla.block.BlockProperties.NORTH
import org.sandboxpowered.silica.vanilla.block.BlockProperties.SOUTH
import org.sandboxpowered.silica.vanilla.block.BlockProperties.UP
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WATERLOGGED
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WEST

class GlowLichenBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(DOWN, EAST, NORTH, SOUTH, UP, WEST, WATERLOGGED)
    }

}