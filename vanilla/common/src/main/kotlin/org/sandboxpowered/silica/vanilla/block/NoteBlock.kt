package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.INSTRUMENT
import org.sandboxpowered.silica.vanilla.block.BlockProperties.NOTE
import org.sandboxpowered.silica.vanilla.block.BlockProperties.POWERED

class NoteBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(NOTE, POWERED, INSTRUMENT)
    }
}