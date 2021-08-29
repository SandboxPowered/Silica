package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.block.SilicaBlockProperties.SNOWY
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class GrassBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(SNOWY)
    }

    override fun createDefaultState(baseState: BlockState): BlockState = baseState.set(SNOWY, false)
}