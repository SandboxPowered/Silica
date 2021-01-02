package org.sandboxpowered.silica.world

import org.sandboxpowered.api.state.BlockState
import org.sandboxpowered.api.block.Blocks
import org.sandboxpowered.api.world.BlockFlag
import org.sandboxpowered.api.block.entity.BlockEntity
import org.sandboxpowered.api.entity.Entity
import org.sandboxpowered.api.state.FluidState
import org.sandboxpowered.api.item.ItemStack
import org.sandboxpowered.api.shape.Box
import org.sandboxpowered.api.tags.TagManager
import org.sandboxpowered.api.util.math.Position
import org.sandboxpowered.api.world.World
import org.sandboxpowered.silica.world.util.BlocTree
import java.util.stream.Stream

abstract class SilicaWorld : World {
    private val blocks: BlocTree = BlocTree(WORLD_MIN, WORLD_MIN, WORLD_MIN, WORLD_SIZE)
    private val defaultState = Blocks.AIR.get().baseState
    private val artemisWorld: com.artemis.World? = null

    override fun getBlockState(position: Position): BlockState {
        return blocks[position.x, position.y, position.z]
    }

    override fun setBlockState(position: Position, state: BlockState, vararg flags: BlockFlag): Boolean {
        blocks[position.x, position.y, position.z] = state
        return true
    }

    override fun getBlockEntity(position: Position): BlockEntity? {
        return null
    }

    override fun getFluidState(position: Position): FluidState {
        return getBlockState(position).fluidState
    }

    override fun getEntitiesWithin(box: Box): Stream<Entity> {
        return Stream.empty()
    }

    override fun <T : Entity?> getEntitiesWithin(box: Box, filter: Class<T>): Stream<T> {
        return Stream.empty()
    }

    override fun getWorldTime(): Long {
        return 0
    }

    override fun getTagManager(): TagManager? {
        return null
    }

    override fun spawnItem(x: Double, y: Double, z: Double, stack: ItemStack) {}

    private companion object {
        private const val WORLD_MIN = -33554432
        private const val WORLD_MAX = 33554431
        private const val WORLD_SIZE = - WORLD_MIN + WORLD_MAX + 1
    }
}