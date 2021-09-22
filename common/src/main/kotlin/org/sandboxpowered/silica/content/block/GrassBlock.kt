package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.SNOWY
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class GrassBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(SNOWY)
    }

    override fun createDefaultState(baseState: BlockState): BlockState = baseState.set(SNOWY, false)
}