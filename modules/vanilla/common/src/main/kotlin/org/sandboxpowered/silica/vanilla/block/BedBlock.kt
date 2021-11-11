package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.BED_PART
import org.sandboxpowered.silica.vanilla.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.vanilla.block.BlockProperties.OCCUPIED

class BedBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        super.appendProperties(builder)
        builder.add(OCCUPIED, HORIZONTAL_FACING, BED_PART)
    }
}