package org.sandboxpowered.silica.world

import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.state.fluid.FluidState
import org.sandboxpowered.silica.util.math.Position

interface WorldReader {
    fun getBlockState(pos: Position): BlockState
    fun getFluidState(pos: Position): FluidState
}