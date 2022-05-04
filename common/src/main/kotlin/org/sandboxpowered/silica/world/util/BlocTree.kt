package org.sandboxpowered.silica.world.util

import net.mostlyoriginal.api.utils.pooling.ObjectPool
import net.mostlyoriginal.api.utils.pooling.Poolable
import org.joml.Vector3fc
import org.sandboxpowered.silica.api.util.extensions.getPool
import org.sandboxpowered.silica.api.util.extensions.pow
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.util.math.ChunkPosition
import org.sandboxpowered.silica.api.world.WorldSection
import org.sandboxpowered.silica.api.world.WorldSelection
import org.sandboxpowered.silica.api.world.rayCast
import org.sandboxpowered.silica.api.world.state.block.BlockState
import scala.util.Either
import scala.util.Left
import scala.util.Right
import java.util.*
import kotlin.math.min

/**
 * Octree for optimized queries in 3d space, but for BlockStates
 *
 * @see OcTree for source
 *
 * Notes for sanity :
 * x: width
 * y: height
 * z: depth
 * E: +x
 * W: -x
 * U: +y
 * D: -y
 * S: +z
 * N: -z
 */
@Suppress("unused")
class BlocTree private constructor(
    x: Int, y: Int, z: Int,
    size: Int
) : Poolable, WorldSection {
    private var bounds = Bounds().set(x, y, z, size)
    private var nodes = arrayOfNulls<BlocTree>(8)
    private var containers = arrayOfNulls<BlockState>(8)
    lateinit var default: BlockState
        private set
    var treeDepth = 0
        private set
    private var nonAirBlockStates = 0

    /**
     * Internal constructor for [ObjectPool] use only
     */
    internal constructor() : this(0, 0, 0, 0)

    override val selection: WorldSelection get() = bounds

    internal fun init(
        treeDepth: Int,
        x: Int, y: Int, z: Int,
        size: Int,
        default: BlockState
    ): BlocTree {
        this.treeDepth = treeDepth
        this.bounds.set(x, y, z, size)
        this.default = default
        this.nonAirBlockStates = if (default.isAir) 0 else size.pow(3)

        return this
    }

    internal fun init(
        treeDepth: Int,
        bounds: Bounds,
        default: BlockState
    ): BlocTree {
        this.treeDepth = treeDepth
        this.bounds = bounds
        this.default = default
        this.nonAirBlockStates = if (default.isAir) 0 else bounds.width.pow(3)

        return this
    }

    private fun indexOf(
        x: Int, y: Int, z: Int
    ): Int {
        val midX = bounds.x + bounds.width / 2
        val midY = bounds.y + bounds.height / 2
        val midZ = bounds.z + bounds.depth / 2

        var res = 0
        res = res or when {
            x >= midX -> E
            else -> W
        }

        res = res or when {
            y >= midY -> U
            else -> D
        }

        res = res or when {
            z >= midZ -> S
            else -> N
        }

        return res
    }

    private fun indexOf(
        x: Int, y: Int, z: Int,
        width: Int, height: Int, depth: Int
    ): Int {
        val midX = bounds.x + bounds.width / 2
        val midY = bounds.y + bounds.height / 2
        val midZ = bounds.z + bounds.depth / 2

        var res = 0
        res = res or when {
            x >= midX -> E
            x + width <= midX -> W
            else -> return OUTSIDE
        }

        res = res or when {
            y >= midY -> U
            y + height <= midY -> D
            else -> return OUTSIDE
        }

        res = res or when {
            z >= midZ -> S
            z + depth <= midZ -> N
            else -> return OUTSIDE
        }

        return res
    }

    /**
     * Sets the target position to [state].
     * Throws an [IllegalArgumentException] if the given position is out of bounds.
     */
    override operator fun set(
        x: Int, y: Int, z: Int,
        state: BlockState
    ) {
        require(bounds.contains(x, y, z)) { "Position $x, $y, $z outside of $bounds" }
        internalSet(x, y, z, state)
    }

    /**
     * @return the previous state at given position
     */
    private fun internalSet(
        x: Int, y: Int, z: Int,
        state: BlockState
    ): BlockState {
        val index = indexOf(x, y, z)
        val n = nodes[index]
        val old = if (n != null) {
            val old = n.internalSet(x, y, z, state)
            if (n.shouldShrink()) {
                containers[index] = n.default
                nodes[index] = null
                otPool.free(n)
            }
            old
        } else {
            if (bounds.width > 2) {
                if (blockStateAtIndex(index) != state) {
                    this.split(index)
                    nodes[index]!!.internalSet(x, y, z, state)
                } else state
            } else {
                val old = this.containers[index] ?: this.default
                this.containers[index] = if (state != this.default) state else null
                old
            }
        }
        if (old.isAir && !state.isAir) ++nonAirBlockStates
        else if (!old.isAir && state.isAir) --nonAirBlockStates
        return old
    }

    /**
     * Checks whether this [BlocTree] should shrink, and update the default when it should
     */
    private fun shouldShrink(): Boolean {
        return if (nodes.all { it == null }) {
            val state = containers[0]
            for (i in 1 until containers.size) if (state !== containers[i]) return false
            this.default = state ?: default
            true
        } else false
    }

    private fun split(index: Int) {
        nodes[index] = pooled(
            treeDepth + 1,
            splitBounds(index),
            blockStateAtIndex(index)
        )
    }

    private fun splitBounds(index: Int): Bounds = Bounds().apply {
        val halfSize = bounds.depth / 2

        when (index) {
            DNW -> set(
                bounds.x, bounds.y, bounds.z,
                halfSize,
            )
            DNE -> set(
                bounds.x + halfSize, bounds.y, bounds.z,
                halfSize,
            )
            DSW -> set(
                bounds.x, bounds.y, bounds.z + halfSize,
                halfSize,
            )
            DSE -> set(
                bounds.x + halfSize, bounds.y, bounds.z + halfSize,
                halfSize,
            )
            UNW -> set(
                bounds.x, bounds.y + halfSize, bounds.z,
                halfSize,
            )
            UNE -> set(
                bounds.x + halfSize, bounds.y + halfSize, bounds.z,
                halfSize,
            )
            USW -> set(
                bounds.x, bounds.y + halfSize, bounds.z + halfSize,
                halfSize,
            )
            USE -> set(
                bounds.x + halfSize, bounds.y + halfSize, bounds.z + halfSize,
                halfSize,
            )
        }
    }

    /**
     * Returns the state at given position
     */
    override operator fun get(x: Int, y: Int, z: Int): BlockState =
        if (bounds.contains(x, y, z)) {
            val index = indexOf(x, y, z)
            nodes[index]?.get(x, y, z) ?: blockStateAtIndex(index)
        } else default

    /**
     * Returns the smallest [BlocTree] containing the selected region
     */
    override operator fun get(
        x: Int, y: Int, z: Int,
        width: Int, height: Int, depth: Int
    ): BlocTree {
        val index = indexOf(x, y, z, width, height, depth)
        return if (index == OUTSIDE || this.nodes[index] == null) this
        else this.nodes[index]!![x, y, z, width, height, depth]
    }

    override fun nonAirInSection(x: Int, y: Int, z: Int): Int {
        require(bounds.contains(x, y, z)) { "Position $x, $y, $z outside of $bounds" }
        require((bounds.x - x) % ChunkPosition.CHUNK_SIZE == 0 && (bounds.y - y) % ChunkPosition.CHUNK_SIZE == 0 && (bounds.z - z) % ChunkPosition.CHUNK_SIZE == 0) { "Position $x, $y, $z is not a chunk origin" }
        return if (bounds.width <= ChunkPosition.CHUNK_SIZE) this.nonAirBlockStates
        else {
            val index = indexOf(x, y, z, ChunkPosition.CHUNK_SIZE, ChunkPosition.CHUNK_SIZE, ChunkPosition.CHUNK_SIZE)
            val n = if (index in 0 until 8) nodes[index] else null
            n?.nonAirInSection(x, y, z) ?: if (
                (if (index in 0 until 8) blockStateAtIndex(index) else default).isAir
            ) 0 else ChunkPosition.CHUNK_CUBE
        }
    }

    /**
     * Reset the [BlocTree] by removing all nodes
     */
    override fun reset() {
        containers.fill(null)
        for (i in nodes.indices) {
            val n = nodes[i]
            if (n != null) {
                n.reset()
                otPool.free(n)
                nodes[i] = null
            }
        }
    }

    override fun rayCast(from: Vector3fc, ray: Vector3fc, max: Float): Float {
        val self = bounds.rayCast(from, ray)
        if (self < 0f || self > max) return -1f

        // This whole thing would've been so fancy in functional style
        // But that would have introduced boxing. Thanks, JVM !
        var min = -1f
        for (i in nodes.indices) {
            val node = nodes[i]
            val hit = node
                ?.rayCast(from, ray, max)
                ?: if (blockStateAtIndex(i).isAir) -1f
                else splitBounds(i).rayCast(from, ray) // TODO: handle non-full & passthrough blocks
            when {
                hit == 0f -> return 0f
                hit > 0f && hit <= max -> min = if (min < 0f) hit else min(min, hit)
            }
        }
        return min
    }

    private fun blockStateAtIndex(index: Int) = containers[index] ?: default

    /**
     * Dispose of the [BlocTree] by removing all nodes and stored ids
     */
    fun dispose() = reset()

    internal fun read(from: Queue<Either<BlockState, Unit>>) {
        reset()
        nonAirBlockStates = if (default.isAir) 0 else bounds.width.pow(3)
        val containerSize = (bounds.width / 2).pow(3)
        var data = from.poll()
        var index = 0
        while (data != null && index < 8) {
            when (data) {
                is Left -> {
                    containers[index] = data.value()

                    if (default.isAir && !data.value().isAir) nonAirBlockStates += containerSize
                    else if (!default.isAir && data.value().isAir) nonAirBlockStates -= containerSize
                }
                else -> {
                    split(index)
                    this.nonAirBlockStates -= nodes[index]!!.nonAirBlockStates
                    nodes[index]?.read(from)
                    this.nonAirBlockStates += nodes[index]!!.nonAirBlockStates
                }
            }
            if (++index < 8) data = from.poll()
        }
    }

    internal fun serialize(): Sequence<Either<BlockState, Unit>> = sequence {
        repeat(8) {
            when (val n = nodes[it]) {
                null -> {
                    yield(Left(blockStateAtIndex(it)))
                }
                else -> {
                    yield(Right(Unit))
                    yieldAll(n.serialize())
                }
            }
        }
    }

    override fun toString() = "BlocTree(depth=$treeDepth, $bounds)"

    companion object {
        private const val OUTSIDE = -1
        private const val D = 0b000
        private const val U = 0b100
        private const val S = 0b000
        private const val N = 0b010
        private const val W = 0b000
        private const val E = 0b001
        private const val DSW = D or S or W
        private const val DSE = D or S or E
        private const val DNW = D or N or W
        private const val DNE = D or N or E
        private const val USW = U or S or W
        private const val USE = U or S or E
        private const val UNW = U or N or W
        private const val UNE = U or N or E

        private val otPool: ObjectPool<BlocTree> get() = getPool()

        private val logger = getLogger<BlocTree>()

        private fun pooled(
            depth: Int,
            x: Int, y: Int, z: Int,
            size: Int, default: BlockState
        ): BlocTree = otPool.obtain().init(depth, x, y, z, size, default)

        private fun pooled(
            depth: Int,
            bounds: Bounds,
            default: BlockState
        ): BlocTree = otPool.obtain().init(depth, bounds, default)

        operator fun invoke(
            x: Int, y: Int, z: Int,
            size: Int, default: BlockState
        ): BlocTree {
            return BlocTree(x, y, z, size).apply { this.default = default }
        }
    }
}

inline fun iterateCube(x: Int, y: Int, z: Int, w: Int, h: Int = w, d: Int = w, iter: (x: Int, y: Int, z: Int) -> Unit) {
    repeat(w) { dx ->
        repeat(h) { dy ->
            repeat(d) { dz ->
                iter(x + dx, y + dy, z + dz)
            }
        }
    }
}
