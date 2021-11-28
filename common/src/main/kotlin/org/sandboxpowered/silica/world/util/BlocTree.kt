package org.sandboxpowered.silica.world.util

import net.mostlyoriginal.api.utils.pooling.ObjectPool
import net.mostlyoriginal.api.utils.pooling.Poolable
import org.sandboxpowered.silica.api.util.extensions.getPool
import org.sandboxpowered.silica.api.util.extensions.pow
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.world.WorldSection
import org.sandboxpowered.silica.api.world.WorldSelection
import org.sandboxpowered.silica.api.world.state.block.BlockState
import scala.util.Either
import scala.util.Left
import scala.util.Right
import java.util.*

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

    private fun indexOf(
        x: Int, y: Int, z: Int
    ): Int {
        val midX = bounds.x + bounds.width / 2
        val midY = bounds.y + bounds.height / 2
        val midZ = bounds.z + bounds.length / 2

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
        val midZ = bounds.z + bounds.length / 2

        var res = 0
        res = res or when {
            x >= midX -> E
            x < midX && x + width <= midX -> W
            else -> return OUTSIDE
        }

        res = res or when {
            y >= midY -> U
            y < midY && y + height <= midY -> D
            else -> return OUTSIDE
        }

        res = res or when {
            z >= midZ -> S
            z < midZ && z + depth <= midZ -> N
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
                if ((containers[index] ?: default) != state) {
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
        val halfSize = bounds.length / 2

        when (index) {
            DNW -> nodes[DNW] = pooled(
                treeDepth + 1,
                bounds.x, bounds.y, bounds.z,
                halfSize,
                containers[DNW] ?: default
            )
            DNE -> nodes[DNE] = pooled(
                treeDepth + 1,
                bounds.x + halfSize, bounds.y, bounds.z,
                halfSize,
                containers[DNE] ?: default
            )
            DSW -> nodes[DSW] = pooled(
                treeDepth + 1,
                bounds.x, bounds.y, bounds.z + halfSize,
                halfSize,
                containers[DSW] ?: default
            )
            DSE -> nodes[DSE] = pooled(
                treeDepth + 1,
                bounds.x + halfSize, bounds.y, bounds.z + halfSize,
                halfSize,
                containers[DSE] ?: default
            )
            UNW -> nodes[UNW] = pooled(
                treeDepth + 1,
                bounds.x, bounds.y + halfSize, bounds.z,
                halfSize,
                containers[UNW] ?: default
            )
            UNE -> nodes[UNE] = pooled(
                treeDepth + 1,
                bounds.x + halfSize, bounds.y + halfSize, bounds.z,
                halfSize,
                containers[UNE] ?: default
            )
            USW -> nodes[USW] = pooled(
                treeDepth + 1,
                bounds.x, bounds.y + halfSize, bounds.z + halfSize,
                halfSize,
                containers[USW] ?: default
            )
            USE -> nodes[USE] = pooled(
                treeDepth + 1,
                bounds.x + halfSize, bounds.y + halfSize, bounds.z + halfSize,
                halfSize,
                containers[USE] ?: default
            )
        }
    }

    /**
     * Returns the state at given position
     */
    override operator fun get(x: Int, y: Int, z: Int): BlockState =
        if (bounds.contains(x, y, z)) {
            val index = indexOf(x, y, z)
            nodes[index]?.get(x, y, z) ?: containers[index] ?: default
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
        require((bounds.x - x) % 16 == 0 && (bounds.z - z) % 16 == 0 && (bounds.z - z) % 16 == 0) { "Position $x, $y, $z is not a chunk origin" }
        return if (bounds.width <= 16) this.nonAirBlockStates
        else {
            val index = indexOf(x, y, z, 16, 16, 16)
            val n = if (index in 0 until 8) nodes[index] else null
            n?.nonAirInSection(x, y, z) ?: if (
                (if (index in 0 until 8) containers[index] ?: default else default).isAir
            ) 0 else 4096
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

    /**
     * Dispose of the [BlocTree] by removing all nodes and stored ids
     */
    fun dispose() = reset()

    internal fun read(from: Queue<Either<BlockState, Unit>>) {
        var data = from.poll()
        var index = 0
        while (data != null && index < 8) {
            when (data) {
                is Left -> containers[index++] = data.value()
                else -> {
                    split(index)
                    nodes[index++]?.read(from)
                }
            }
            if (index < 8) data = from.poll()
        }
    }

    internal fun serialize(): Sequence<Either<BlockState, Unit>> = sequence {
        repeat(8) {
            when (val n = nodes[it]) {
                null -> {
                    @Suppress("RemoveExplicitTypeArguments") // it's a lie
                    yield(Left<BlockState, Unit>(containers[it] ?: default))
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
