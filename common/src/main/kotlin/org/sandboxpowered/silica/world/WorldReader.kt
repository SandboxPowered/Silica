package org.sandboxpowered.silica.world

import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.world.state.block.BlockState
import org.sandboxpowered.silica.world.state.fluid.FluidState

interface WorldReader {
    fun getBlockState(pos: Position): BlockState
    fun getFluidState(pos: Position): FluidState
}