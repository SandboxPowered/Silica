package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.INSTRUMENT
import org.sandboxpowered.silica.content.block.BlockProperties.NOTE
import org.sandboxpowered.silica.content.block.BlockProperties.POWERED
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

class NoteBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(NOTE, POWERED, INSTRUMENT)
    }
}