package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.Identifier
import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.SNOWY

class GrassBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(SNOWY)
    }

    override fun createDefaultState(baseState: BlockState): BlockState = baseState.set(SNOWY, false)
}