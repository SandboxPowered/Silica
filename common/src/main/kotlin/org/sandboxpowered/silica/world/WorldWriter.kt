package org.sandboxpowered.silica.world

import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.world.state.block.BlockState

interface WorldWriter {
    fun setBlockState(pos: Position, state: BlockState)
}