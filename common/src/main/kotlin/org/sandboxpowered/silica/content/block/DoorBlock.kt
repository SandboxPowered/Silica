package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.DOOR_HALF
import org.sandboxpowered.silica.content.block.BlockProperties.DOOR_HINGE
import org.sandboxpowered.silica.content.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.content.block.BlockProperties.OPEN
import org.sandboxpowered.silica.content.block.BlockProperties.POWERED
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

class DoorBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(HORIZONTAL_FACING, OPEN, POWERED, DOOR_HINGE, DOOR_HALF)
    }
}