package org.sandboxpowered.silica.world.util

import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.mostlyoriginal.api.utils.pooling.ObjectPool
import org.sandboxpowered.silica.api.util.extensions.getPool
import org.sandboxpowered.silica.api.util.extensions.onException
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.world.WorldSection
import org.sandboxpowered.silica.api.world.WorldSelection
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.api.world.walk
import java.util.concurrent.CompletionStage
import java.util.concurrent.TimeoutException
import kotlin.math.floor

fun interface GenerateChunk {
    operator fun invoke(x: Int, y: Int, z: Int, chunk: BlocTree): CompletionStage<BlocTree>
}

class WorldData(
    override val selection: WorldSelection,
    private val default: BlockState,
    private val generate: GenerateChunk
) : WorldSection {
    private val logger = getLogger()

    private val placeHolder = BlocTree()
    private val storage: Object2ObjectMap<Key, BlocTree> = Object2ObjectOpenHashMap()

    private fun Int.alignToChunk() = floor(this / 16f).toInt() * 16

    // TODO: ideally this would be in an actor for proper concurrency handling
    fun load(selection: WorldSelection) {
        selection.walk(16) { x, y, z ->
            if (this.contains(x, y, z)) {
                val ax = x.alignToChunk()
                val ay = y.alignToChunk()
                val az = z.alignToChunk()
                val key = Key(ax, ay, az)
                if (key !in storage) {
                    // Serves both as a "generating chunks" marker and to make sure storage is grown in the correct thread
                    storage[key] = placeHolder
                    generate(ax, ay, az, pooled(ax, ay, az, 16, default)).thenAccept {
                        storage[key] = it
                    }.onException {
                        when (it.cause) {
                            is TimeoutException -> logger.warn("Unable to generate chunk at $ax, $ay, $az in time")
                            else -> logger.warn("Unable to generate chunk at $ax, $ay, $az", it)
                        }
                    }
                }
            }
        }
    }

    fun unload(selection: WorldSelection) {
        selection.walk(16) { x, y, z ->
            if (this.contains(x, y, z)) {
                storage.remove(Key(x, y, z))?.free()
            }
        }
    }

    private fun chunk(x: Int, y: Int, z: Int) = storage[Key(x, y, z)]

    override fun set(x: Int, y: Int, z: Int, state: BlockState) {
        storage[Key(x, y, z)]?.set(x, y, z, state)
    }

    override fun get(x: Int, y: Int, z: Int, width: Int, height: Int, depth: Int): WorldSection {
        TODO("Not yet implemented")
    }

    override fun get(x: Int, y: Int, z: Int): BlockState = storage[Key(x, y, z)]?.get(x, y, z) ?: default

    override fun nonAirInSection(x: Int, y: Int, z: Int) = storage[Key(x, y, z)]?.nonAirInSection(x, y, z) ?: 0

    private class Key(x: Int, y: Int, z: Int) {
        private val coarseX = x.toUInt() and ((1u shl TREES_DEPTH) - 1u).inv()
        private val coarseY = y.toUInt() and ((1u shl TREES_DEPTH) - 1u).inv()
        private val coarseZ = z.toUInt() and ((1u shl TREES_DEPTH) - 1u).inv()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Key) return false

            if (coarseX != other.coarseX) return false
            if (coarseY != other.coarseY) return false
            if (coarseZ != other.coarseZ) return false

            return true
        }

        override fun hashCode(): Int {
            return ((coarseX * 73856093u) xor (coarseY * 19349663u) xor (coarseZ * 83492791u)).toInt()
        }
    }

    private fun pooled(
        x: Int, y: Int, z: Int,
        size: Int, default: BlockState
    ): BlocTree = otPool.obtain().init(0, x, y, z, size, default)

    private val otPool: ObjectPool<BlocTree> get() = getPool()

    private fun BlocTree.free() = otPool.free(this)

    private companion object {
        private const val TREES_DEPTH = 4
    }
}
