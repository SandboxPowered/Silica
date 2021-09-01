package org.sandboxpowered.silica.world

import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.math.Position

interface WorldWriter {
    fun setBlockState(pos: Position, state: BlockState)
}