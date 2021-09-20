package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.block.SilicaBlockProperties.HALF
import org.sandboxpowered.silica.block.SilicaBlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.block.SilicaBlockProperties.OPEN
import org.sandboxpowered.silica.block.SilicaBlockProperties.POWERED
import org.sandboxpowered.silica.block.SilicaBlockProperties.WATERLOGGED
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class TrapdoorBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(HORIZONTAL_FACING, OPEN, POWERED, HALF, WATERLOGGED)
    }
}