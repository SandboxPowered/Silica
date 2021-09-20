package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.block.SilicaBlockProperties.DISTANCE_1_7
import org.sandboxpowered.silica.block.SilicaBlockProperties.PERSISTENT
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class LeavesBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(DISTANCE_1_7, PERSISTENT)
    }
}