package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.block.SilicaBlockProperties.BED_PART
import org.sandboxpowered.silica.block.SilicaBlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.block.SilicaBlockProperties.OCCUPIED
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class BedBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(OCCUPIED, HORIZONTAL_FACING, BED_PART)
    }
}