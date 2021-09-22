package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.DISTANCE_1_7
import org.sandboxpowered.silica.content.block.BlockProperties.PERSISTENT
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class LeavesBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(DISTANCE_1_7, PERSISTENT)
    }
}