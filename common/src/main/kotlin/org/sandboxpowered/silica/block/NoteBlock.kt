package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class NoteBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(SilicaBlockProperties.NOTE, SilicaBlockProperties.POWERED, SilicaBlockProperties.INSTRUMENT)
    }
}