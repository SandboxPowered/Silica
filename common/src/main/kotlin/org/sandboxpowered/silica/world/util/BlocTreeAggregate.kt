package org.sandboxpowered.silica.world.util

import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.joml.Vector3fc
import org.sandboxpowered.silica.api.util.extensions.component1
import org.sandboxpowered.silica.api.util.extensions.component2
import org.sandboxpowered.silica.api.util.extensions.component3
import org.sandboxpowered.silica.api.util.math.ChunkPosition
import org.sandboxpowered.silica.api.util.math.chunkAligned
import org.sandboxpowered.silica.api.world.WorldSection
import org.sandboxpowered.silica.api.world.WorldSelection
import org.sandboxpowered.silica.api.world.rayCast
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.api.world.walk
import org.sandboxpowered.utilities.math.times
import org.sandboxpowered.utilities.math.x
import org.sandboxpowered.utilities.math.y
import org.sandboxpowered.utilities.math.z
import kotlin.math.min

open class BlocTreeAggregate(
    final override val selection: WorldSelection,
    protected val default: BlockState,
) : WorldSection {
    protected val placeHolder =
        BlocTree().init(0, Int.MIN_VALUE / 2, Int.MIN_VALUE / 2, Int.MIN_VALUE / 2, Int.MAX_VALUE, default)

    protected val storage: Object2ObjectMap<ChunkKey, BlocTree> = Object2ObjectOpenHashMap()

    protected open operator fun contains(key: ChunkKey): Boolean = key in storage

    override fun get(x: Int, y: Int, z: Int): BlockState {
        val key = ChunkKey(x, y, z)
        return if (key in this) storage[key]!![x, y, z] else default
    }

    override fun set(x: Int, y: Int, z: Int, state: BlockState) {
        val key = ChunkKey(x, y, z)
        if (key in this) storage[key]!![x, y, z] = state
    }

    private fun isInSingleChunk(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int) =
        x.mod(ChunkPosition.CHUNK_SIZE) + width <= 16
                && y.mod(ChunkPosition.CHUNK_SIZE) + height <= 16
                && z.mod(ChunkPosition.CHUNK_SIZE) + depth <= 16

    override fun get(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int): WorldSection {
        if (isInSingleChunk(x, y, z, width, height, depth)) {
            val key = ChunkKey(x, y, z)
            return if (key in this) storage[key]!! else placeHolder
        }
        val bounds = Bounds().set(
            x.chunkAligned,
            y.chunkAligned,
            z.chunkAligned,
            width,
            height,
            depth
        ) // FIXME: crashing cuz somehow we can get negative bounds whd ?
        require(bounds in selection) { "Query for $bounds out of $selection" }
        if (bounds == selection || Bounds().set(
                selection.x, selection.y, selection.z,
                selection.width.chunkAligned,
                selection.height.chunkAligned,
                selection.depth.chunkAligned
            ) in bounds
        ) return this
        val chunks = Object2ObjectOpenHashMap<ChunkKey, BlocTree>().apply {
            bounds.walk(16) { xi, yi, zi ->
                val key = ChunkKey(xi, yi, zi)
                if (key in this) set(key, storage[key]!!)
            }
        }
        return if (chunks.size == 1) chunks.values.single()
        else BlocTreeAggregate(bounds, default).also {
            it.storage += chunks
        }
    }

    override fun nonAirInSection(x: Int, y: Int, z: Int): Int {
        val key = ChunkKey(x, y, z)
        return when {
            key in this -> storage[key]!!.nonAirInSection(x, y, z)
            default.isAir -> 0
            else -> ChunkPosition.CHUNK_SIZE * ChunkPosition.CHUNK_SIZE * ChunkPosition.CHUNK_SIZE
        }
    }

    override fun rayCast(from: Vector3fc, ray: Vector3fc, max: Float): Float {
        val self = selection.rayCast(from, ray)
        if (self < 0f || self > max) return -1f
        val (mx, my, mz) = ray * max
        val subSection = get(
            from.x.toInt(), from.y.toInt(), from.z.toInt(),
            mx.toInt() + 1, my.toInt() + 1, mz.toInt() + 1
        )
        return if (subSection == this) {
            var min = -1f
            storage.values.forEach { blocTree ->
                val hit = blocTree.rayCast(from, ray, max)
                when {
                    hit == 0f -> return 0f
                    hit > 0f && hit <= max -> min = if (min < 0f) hit else min(min, hit)
                }
            }
            min
        } else subSection.rayCast(from, ray, max)
    }
}

