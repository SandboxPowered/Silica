package org.sandboxpowered.silica.world.util

import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.sandboxpowered.silica.api.util.extensions.onException
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.world.WorldSection
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.world.util.BlocTree.Companion.free
import java.util.concurrent.CompletionStage

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

    // TODO: ideally this would be in an actor for proper concurrency handling
    fun load(selection: WorldSelection) {
        selection.walk(16) { x, y, z ->
            if (this.contains(x, y, z)) {
                val key = Key(x, y, z)
                if (key !in storage) {
                    // Serves both as a "generating chunks" marker and to make sure storage is grown in the correct thread
                    storage[key] = placeHolder
                    generate(x, y, z, BlocTree.pooled(x, y, z, 16, default)).thenAccept {
                        logger.info("Received $x $y $z")
                        storage[key] = it
                    }.onException { logger.warn("Unable to generate chunk at $x, $y, $z"/*, it*/) }
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

    private companion object {
        private const val TREES_DEPTH = 4
    }
}
