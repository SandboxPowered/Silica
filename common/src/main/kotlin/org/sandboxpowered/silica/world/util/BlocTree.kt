package org.sandboxpowered.silica.world.util

import net.mostlyoriginal.api.utils.pooling.ObjectPool
import net.mostlyoriginal.api.utils.pooling.Poolable
import org.sandboxpowered.api.state.BlockState
import org.sandboxpowered.silica.util.getPool

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
) : Poolable {
    private var bounds = Bounds().set(x, y, z, size)
    private var nodes = arrayOfNulls<BlocTree>(8)
    private var containers = arrayOfNulls<BlockState>(8)
    private lateinit var default: BlockState
    var treeDepth = 0
        private set
    private var parent: BlocTree? = null

    /**
     * Internal constructor for [ObjectPool] use only
     */
    internal constructor() : this(0, 0, 0, 0)

    private fun init(
        treeDepth: Int,
        x: Int, y: Int, z: Int,
        size: Int,
        parent: BlocTree,
        default: BlockState
    ): BlocTree {
        this.treeDepth = treeDepth
        this.bounds.set(x, y, z, size)
        this.parent = parent
        this.default = default

        return this
    }

    private fun indexOf(
        x: Int, y: Int, z: Int
    ): Int {
        val midX = bounds.x + bounds.size / 2
        val midY = bounds.y + bounds.size / 2
        val midZ = bounds.z + bounds.size / 2

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
        val midX = bounds.x + bounds.size / 2
        val midY = bounds.y + bounds.size / 2
        val midZ = bounds.z + bounds.size / 2

        var res = 0
        res = res or when {
            x > midX -> E
            x < midX && x + width < midX -> W
            else -> return OUTSIDE
        }

        res = res or when {
            y > midY -> U
            y < midY && y + height < midY -> D
            else -> return OUTSIDE
        }

        res = res or when {
            z > midZ -> S
            z < midZ && z + depth < midZ -> N
            else -> return OUTSIDE
        }

        return res
    }

    /**
     * Sets the target position to [state].
     * Throws an [IllegalArgumentException] if the given position is out of bounds.
     */
    operator fun set(
        x: Int, y: Int, z: Int,
        state: BlockState
    ) {
        require(bounds.contains(x, y, z)) { "Position $x, $y, $z outside of $bounds" }
        internalSet(x, y, z, state)
    }

    private fun internalSet(
        x: Int, y: Int, z: Int,
        state: BlockState
    ) {
        val index = indexOf(x, y, z)
        val n = nodes[index]
        if (n != null) {
            n.internalSet(x, y, z, state)
            if (n.shouldShrink()) {
                containers[index] = n.default
                nodes[index] = null
                otPool.free(n)
            }
        } else {
            if (bounds.size > 2) {
                if ((containers[index] ?: default) != state) {
                    this.split(index)
                    nodes[index]!!.internalSet(x, y, z, state)
                }
            } else {
                this.containers[index] = if (state != this.default) state else null
            }
        }
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
        val halfSize = bounds.size / 2

        when (index) {
            DNW -> nodes[DNW] = otPool.obtain().init(
                treeDepth + 1,
                bounds.x, bounds.y, bounds.z,
                halfSize,
                this, this.containers[DNW] ?: this.default
            )
            DNE -> nodes[DNE] = otPool.obtain().init(
                treeDepth + 1,
                bounds.x + halfSize, bounds.y, bounds.z,
                halfSize,
                this, this.containers[DNE] ?: this.default
            )
            DSW -> nodes[DSW] = otPool.obtain().init(
                treeDepth + 1,
                bounds.x, bounds.y, bounds.z + halfSize,
                halfSize,
                this, this.containers[DSW] ?: this.default
            )
            DSE -> nodes[DSE] = otPool.obtain().init(
                treeDepth + 1,
                bounds.x + halfSize, bounds.y, bounds.z + halfSize,
                halfSize,
                this, this.containers[DSE] ?: this.default
            )
            UNW -> nodes[UNW] = otPool.obtain().init(
                treeDepth + 1,
                bounds.x, bounds.y + halfSize, bounds.z,
                halfSize,
                this, this.containers[UNW] ?: this.default
            )
            UNE -> nodes[UNE] = otPool.obtain().init(
                treeDepth + 1,
                bounds.x + halfSize, bounds.y + halfSize, bounds.z,
                halfSize,
                this, this.containers[UNE] ?: this.default
            )
            USW -> nodes[USW] = otPool.obtain().init(
                treeDepth + 1,
                bounds.x, bounds.y + halfSize, bounds.z + halfSize,
                halfSize,
                this, this.containers[USW] ?: this.default
            )
            USE -> nodes[USE] = otPool.obtain().init(
                treeDepth + 1,
                bounds.x + halfSize, bounds.y + halfSize, bounds.z + halfSize,
                halfSize,
                this, this.containers[USE] ?: this.default
            )
        }
    }

    /**
     * Returns the state at given position
     */
    operator fun get(x: Int, y: Int, z: Int): BlockState {
        if (bounds.contains(x, y, z)) {
            val index = indexOf(x, y, z)
            return nodes[index]?.get(x, y, z) ?: containers[index] ?: default
        }
        return default
    }

    /**
     * Returns the smallest [BlocTree] containing the selected region
     */
    operator fun get(
        x: Int, y: Int, z: Int,
        width: Int, height: Int, depth: Int
    ): BlocTree {
        val index = indexOf(x, y, z, width, height, depth)
        return if (index == OUTSIDE || this.nodes[index] == null) this
        else this.nodes[index]!!
    }

    /**
     * Reset the [BlocTree] by removing all nodes
     */
    override fun reset() {
        containers.fill(null)
        for (i in nodes.indices) if (nodes[i] != null) {
            otPool.free(nodes[i])
            nodes[i] = null
        }
    }

    /**
     * Dispose of the [BlocTree] by removing all nodes and stored ids
     */
    fun dispose() = reset()

    override fun toString() = "BlocTree(depth=$treeDepth, $bounds)"

    /**
     * Simple square AABB
     */
    private class Bounds {
        var x = 0
            private set
        var y = 0
            private set
        var z = 0
            private set
        var size = 0
            private set

        fun set(
            x: Int, y: Int, z: Int,
            size: Int
        ): Bounds {
            this.x = x
            this.y = y
            this.z = z
            this.size = size
            return this
        }

        fun contains(x: Int, y: Int, z: Int): Boolean =
            this.x <= x && this.x + this.size > x
                    && this.y <= y && this.y + this.size > y
                    && this.z <= z && this.z + this.size > z

        override fun toString() = "Bounds(x=$x, y=$y, z=$z, size=$size)"
    }

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

        private val otPool: ObjectPool<BlocTree> = getPool()

        operator fun invoke(
            x: Int, y: Int, z: Int,
            size: Int, default: BlockState
        ): BlocTree {
            return BlocTree(x, y, z, size).apply { this.default = default }
        }
    }
}