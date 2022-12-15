package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.POWER
import org.sandboxpowered.silica.vanilla.block.BlockProperties.REDSTONE_SIDE_EAST
import org.sandboxpowered.silica.vanilla.block.BlockProperties.REDSTONE_SIDE_NORTH
import org.sandboxpowered.silica.vanilla.block.BlockProperties.REDSTONE_SIDE_SOUTH
import org.sandboxpowered.silica.vanilla.block.BlockProperties.REDSTONE_SIDE_WEST

class RedstoneWireBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(REDSTONE_SIDE_NORTH, REDSTONE_SIDE_EAST, REDSTONE_SIDE_SOUTH, REDSTONE_SIDE_WEST, POWER)
    }
}