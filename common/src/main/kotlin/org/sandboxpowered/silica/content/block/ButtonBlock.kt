package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.BUTTON_FACE
import org.sandboxpowered.silica.content.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.content.block.BlockProperties.POWERED
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

class ButtonBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(POWERED, HORIZONTAL_FACING, BUTTON_FACE)
    }
}