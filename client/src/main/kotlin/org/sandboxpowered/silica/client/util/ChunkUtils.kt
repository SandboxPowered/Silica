package org.sandboxpowered.silica.client.util

import it.unimi.dsi.fastutil.objects.Object2ObjectMap
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import org.joml.Vector3d
import org.joml.Vector3i
import org.sandboxpowered.silica.world.util.BlocTree
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.floor

class ChunkUtils {
    private fun playerInsideChunk(chunk: RenderChunk): Boolean {
        val margin = CHUNK_SIZE * 0.5f
        val minX = chunk.cx shl CHUNK_SIZE_SHIFT
        val maxX = minX + CHUNK_SIZE
        val minZ = chunk.cz shl CHUNK_SIZE_SHIFT
        val maxZ = minZ + CHUNK_SIZE
        return playerPosition.x + margin >= minX && playerPosition.x - margin <= maxX && playerPosition.z + margin >= minZ && playerPosition.z - margin <= maxZ
    }

    class RenderChunk(val cx: Int, val cy: Int, val cz: Int) {
        var neighbors = 0
        private var built = false
    }

    companion object {
        private const val EMPTY_VOXEL: Byte = 0
        private const val CHUNK_SIZE_SHIFT = 5 // 4 - 16, 5 - 32
        private const val CHUNK_SIZE = 1 shl CHUNK_SIZE_SHIFT
        private const val MAX_ACTIVE_CHUNKS = 65536
        private const val MAX_RENDER_DISTANCE_CHUNKS = 40
        private const val MAX_RENDER_DISTANCE_METERS = MAX_RENDER_DISTANCE_CHUNKS shl CHUNK_SIZE_SHIFT
        private val chunkByCoordinate: Object2ObjectMap<Vector3i, RenderChunk> = Object2ObjectOpenHashMap()
        private val allChunks: MutableList<RenderChunk> = ArrayList()
        private val frontierChunks: MutableList<RenderChunk> = ArrayList()
        private val executorService =
            Executors.newFixedThreadPool(4.coerceAtLeast(Runtime.getRuntime().availableProcessors() / 2)) { r ->
                val thread = Thread(r)
                thread.priority = Thread.MIN_PRIORITY
                thread.name = "Chunk builder"
                thread.isDaemon = true
                thread
            }
        private val chunkBuildTasksCount = AtomicInteger()
        private const val nxX = 0f
        private const val nxY = 0f
        private const val nxZ = 0f
        private const val nxW = 0f
        private const val pxX = 0f
        private const val pxY = 0f
        private const val pxZ = 0f
        private const val pxW = 0f
        private const val nyX = 0f
        private const val nyY = 0f
        private const val nyZ = 0f
        private const val nyW = 0f
        private const val pyX = 0f
        private const val pyY = 0f
        private const val pyZ = 0f
        private const val pyW = 0f
        private val playerPosition: Vector3d = Vector3d()
        private val inView = Comparator.comparing { chunk: RenderChunk -> chunkNotInFrustum(chunk) }
        private val byDistance = Comparator.comparingDouble { chunk: RenderChunk -> distToChunk(chunk) }
        private val inViewAndDistance = inView.thenComparing(byDistance)
        fun shutdown() {
            executorService.shutdown()
            try {
                if (!executorService.awaitTermination(2000L, TimeUnit.MILLISECONDS)) throw AssertionError()
            } catch (e: Exception) {
                throw AssertionError()
            }
        }

        fun createChunk(tree: BlocTree, x: Int, y: Int, z: Int): RenderChunk {
            val chunk = RenderChunk(x, y, z)
            allChunks.add(chunk)
            chunkByCoordinate[Vector3i(x, y, z)] = chunk
            addFreshChunk(chunk)
            val smallestNode = tree[x * CHUNK_SIZE, y * CHUNK_SIZE, z * CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE, CHUNK_SIZE]
            chunkBuildTasksCount.incrementAndGet()
            return chunk
        }

        private fun onChunkRemoved(chunk: RenderChunk): Int {
            val d = distToChunk(chunk.cx, chunk.cy, chunk.cz)
            return (onChunkRemoved(chunk.cx - 1, chunk.cy, chunk.cz, d)
                    + onChunkRemoved(chunk.cx + 1, chunk.cy, chunk.cz, d)
                    + onChunkRemoved(chunk.cx, chunk.cy, chunk.cz - 1, d)
                    + onChunkRemoved(chunk.cx, chunk.cy, chunk.cz + 1, d)
                    + onChunkRemoved(chunk.cx, chunk.cy - 1, chunk.cz, d)
                    + onChunkRemoved(chunk.cx, chunk.cy + 1, chunk.cz, d))
        }

        private fun culledXY(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float): Boolean {
            return nxX * (if (nxX < 0) minX else maxX) + nxY * (if (nxY < 0) minY else maxY) + nxZ * (if (nxZ < 0) minZ else maxZ) < -nxW
                    || pxX * (if (pxX < 0) minX else maxX) + pxY * (if (pxY < 0) minY else maxY) + pxZ * (if (pxZ < 0) minZ else maxZ) < -pxW
                    || nyX * (if (nyX < 0) minX else maxX) + nyY * (if (nyY < 0) minY else maxY) + nyZ * (if (nyZ < 0) minZ else maxZ) < -nyW
                    || pyX * (if (pyX < 0) minX else maxX) + pyY * (if (pyY < 0) minY else maxY) + pyZ * (if (pyZ < 0) minZ else maxZ) < -pyW
        }

        private fun chunkNotInFrustum(chunk: RenderChunk): Boolean {
            val xf = (chunk.cx shl CHUNK_SIZE_SHIFT) - floor(playerPosition.x).toFloat()
            val yf = (chunk.cy shl CHUNK_SIZE_SHIFT) - floor(playerPosition.y).toFloat()
            val zf = (chunk.cz shl CHUNK_SIZE_SHIFT) - floor(playerPosition.z).toFloat()
            return culledXY(xf, yf, zf, xf + CHUNK_SIZE, yf + CHUNK_SIZE, zf + CHUNK_SIZE)
        }

        private fun chunkInRenderDistance(x: Int, y: Int, z: Int): Boolean {
            return distToChunk(x, y, z) < MAX_RENDER_DISTANCE_METERS * MAX_RENDER_DISTANCE_METERS
        }

        private fun onChunkRemoved(cx: Int, cy: Int, cz: Int, d: Double): Int {
            val n = chunkByCoordinate[Vector3i(cx, cy, cz)]
            if (n != null) {
                n.neighbors--
                if (!frontierChunks.contains(n) && (chunkInRenderDistance(cx, cy, cz) || distToChunk(cx, cy, cz) < d)) {
                    frontierChunks.add(n)
                    return 1
                }
            }
            return 0
        }

        private fun distToChunk(chunk: RenderChunk): Double {
            return distToChunk(chunk.cx, chunk.cy, chunk.cz)
        }

        private fun distToChunk(cx: Int, cy: Int, cz: Int): Double {
            val dx = playerPosition.x - (cx + 0.5) * CHUNK_SIZE
            val dy = playerPosition.y - (cy + 0.5) * CHUNK_SIZE
            val dz = playerPosition.z - (cz + 0.5) * CHUNK_SIZE
            return dx * dx + dy * dy + dz * dz
        }

        private fun addFreshChunk(chunk: RenderChunk) {
            frontierChunks.add(chunk)
            updateFrontierChunk(chunk, chunk.cx - 1, chunk.cy, chunk.cz)
            updateFrontierChunk(chunk, chunk.cx + 1, chunk.cy, chunk.cz)
            updateFrontierChunk(chunk, chunk.cx, chunk.cy, chunk.cz - 1)
            updateFrontierChunk(chunk, chunk.cx, chunk.cz, chunk.cz + 1)
            updateFrontierChunk(chunk, chunk.cx, chunk.cy - 1, chunk.cz)
            updateFrontierChunk(chunk, chunk.cx, chunk.cz + 1, chunk.cz)
        }

        private fun updateFrontierChunk(frontier: RenderChunk, cx: Int, cy: Int, cz: Int) {
            val n = chunkByCoordinate[Vector3i(cx, cy, cz)]
            if (n != null) {
                n.neighbors++
                frontier.neighbors++
                if (n.neighbors == 6) {
                    frontierChunks.remove(n)
                }
            }
        }
    }
}