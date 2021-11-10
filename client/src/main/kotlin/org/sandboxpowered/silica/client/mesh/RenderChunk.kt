package org.sandboxpowered.silica.client.mesh

import org.sandboxpowered.silica.api.util.math.Position

data class RenderChunk(val pos: ChunkPos, val vertexData: FloatArray) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RenderChunk

        if (pos != other.pos) return false
        if (!vertexData.contentEquals(other.vertexData)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = pos.hashCode()
        result = 31 * result + vertexData.contentHashCode()
        return result
    }

}

data class ChunkPos(val cx: Int, val cy: Int, val cz: Int) {
    fun toWorldPos(): Position = Position(cx * 16, cy * 16, cz * 16)

}