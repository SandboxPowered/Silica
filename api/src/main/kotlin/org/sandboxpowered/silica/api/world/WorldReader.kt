package org.sandboxpowered.silica.api.world

import org.joml.Vector2ic
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.api.world.state.fluid.FluidState

interface WorldReader {
    val isClient: Boolean
    val isServer: Boolean
    val worldHeight: Vector2ic

    fun getBlockState(pos: Position): BlockState
    fun getFluidState(pos: Position): FluidState

    fun getHeight(): Int = worldHeight.y() - worldHeight.x()

    fun isWithinHeightLimit(pos: Position): Boolean = isOutOfHeightLimit(pos.y)
    fun isWithinHeightLimit(height: Int): Boolean = height in worldHeight.x()..worldHeight.y()

    fun isOutOfHeightLimit(pos: Position): Boolean = isOutOfHeightLimit(pos.y)
    fun isOutOfHeightLimit(height: Int): Boolean = !isWithinHeightLimit(height)

    fun registerEventSubscriber(sub: Any)
}