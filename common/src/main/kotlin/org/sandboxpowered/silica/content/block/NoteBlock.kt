package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.content.block.BlockProperties.INSTRUMENT
import org.sandboxpowered.silica.content.block.BlockProperties.NOTE
import org.sandboxpowered.silica.content.block.BlockProperties.POWERED
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState

class NoteBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(NOTE, POWERED, INSTRUMENT)
    }
}