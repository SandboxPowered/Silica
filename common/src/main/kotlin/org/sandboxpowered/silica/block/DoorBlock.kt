package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.block.SilicaBlockProperties.DOOR_HALF
import org.sandboxpowered.silica.block.SilicaBlockProperties.DOOR_HINGE
import org.sandboxpowered.silica.block.SilicaBlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.block.SilicaBlockProperties.OPEN
import org.sandboxpowered.silica.block.SilicaBlockProperties.POWERED
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class DoorBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(HORIZONTAL_FACING, OPEN, POWERED, DOOR_HINGE, DOOR_HALF)
    }
}