package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.ATTACHED
import org.sandboxpowered.silica.vanilla.block.BlockProperties.DISARMED
import org.sandboxpowered.silica.vanilla.block.BlockProperties.EAST
import org.sandboxpowered.silica.vanilla.block.BlockProperties.NORTH
import org.sandboxpowered.silica.vanilla.block.BlockProperties.POWERED
import org.sandboxpowered.silica.vanilla.block.BlockProperties.SOUTH
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WEST

class TripwireBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(ATTACHED, DISARMED, POWERED, EAST, NORTH, WEST, SOUTH)
    }

}