package org.sandboxpowered.silica.world.util

import org.sandboxpowered.silica.api.util.math.ChunkPosition

class ChunkKey(x: Int, y: Int, z: Int) {
    val coarseX = x.toUInt() and ((1u shl ChunkPosition.TREES_DEPTH) - 1u).inv()
    val coarseY = y.toUInt() and ((1u shl ChunkPosition.TREES_DEPTH) - 1u).inv()
    val coarseZ = z.toUInt() and ((1u shl ChunkPosition.TREES_DEPTH) - 1u).inv()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is ChunkKey) return false

        if (coarseX != other.coarseX) return false
        if (coarseY != other.coarseY) return false
        if (coarseZ != other.coarseZ) return false

        return true
    }

    override fun hashCode(): Int {
        return ((coarseX * 73856093u) xor (coarseY * 19349663u) xor (coarseZ * 83492791u)).toInt()
    }
}