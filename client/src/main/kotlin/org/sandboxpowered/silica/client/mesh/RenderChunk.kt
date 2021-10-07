package org.sandboxpowered.silica.client.mesh

data class RenderChunk(val pos: ChunkPos) {
    var neighbors = 0
}

data class ChunkPos(val cx: Int, val cy: Int, val cz: Int) {

}