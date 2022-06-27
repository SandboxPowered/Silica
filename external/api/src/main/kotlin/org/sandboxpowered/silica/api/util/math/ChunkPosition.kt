package org.sandboxpowered.silica.api.util.math

import org.joml.Vector2i
import org.joml.Vector3dc

data class ChunkPosition(
    val x: Int,
    val y: Int,
    val z: Int
) {
    val xz: Vector2i
        get() = Vector2i(x, z)

    companion object {
        operator fun invoke(position: Position): ChunkPosition = position.toChunkPosition()

        operator fun invoke(vector: Vector3dc): ChunkPosition =
            ChunkPosition(vector.x().toInt() shr 4, vector.y().toInt() shr 4, vector.z().toInt() shr 4)

        const val TREES_DEPTH = 4
        const val CHUNK_SIZE = 1 shl TREES_DEPTH
        const val CHUNK_SIZE_F = CHUNK_SIZE.toFloat()
        const val CHUNK_CUBE = CHUNK_SIZE * CHUNK_SIZE * CHUNK_SIZE
    }
}

/**
 * World coordinate to Chunk-aligned world coordinate
 */
val Int.chunkAligned get() = this shr ChunkPosition.TREES_DEPTH shl ChunkPosition.TREES_DEPTH
