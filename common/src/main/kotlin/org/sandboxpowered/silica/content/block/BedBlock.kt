package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.block.BlockProperties.BED_PART
import org.sandboxpowered.silica.content.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.content.block.BlockProperties.OCCUPIED
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

class BedBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(OCCUPIED, HORIZONTAL_FACING, BED_PART)
    }
}