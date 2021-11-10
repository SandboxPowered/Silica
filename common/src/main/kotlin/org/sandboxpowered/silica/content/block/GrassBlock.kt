package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.content.block.BlockProperties.SNOWY
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState

class GrassBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(SNOWY)
    }

    override fun createDefaultState(baseState: BlockState): BlockState = baseState.set(SNOWY, false)
}