package org.sandboxpowered.silica.world.util

import net.mostlyoriginal.api.utils.pooling.ObjectPool
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.extensions.getPool
import kotlin.math.pow

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
class BlocTree private constructor(x: Int, y: Int, z: Int, size: Int) :
    GenericTree<BlocTree, BlockState>(x, y, z, size) {
    private var nonAirBlockStates = 0

    /**
     * Internal constructor for [ObjectPool] use only
     */
    internal constructor() : this(0, 0, 0, 0)

    override fun init(
        treeDepth: Int,
        x: Int,
        y: Int,
        z: Int,
        size: Int,
        parent: BlocTree,
        default: BlockState
    ): BlocTree {
        super.init(treeDepth, x, y, z, size, parent, default)
        this.nonAirBlockStates = if (default.isAir) 0 else size.toDouble().pow(3).toInt()
        return this
    }

    override fun internalSet(x: Int, y: Int, z: Int, value: BlockState): BlockState {
        val old = super.internalSet(x, y, z, value)

        if (old.isAir && !value.isAir) ++nonAirBlockStates
        else if (!old.isAir && value.isAir) --nonAirBlockStates

        return old
    }

    fun nonAirInChunk(x: Int, y: Int, z: Int): Int {
        require(bounds.contains(x, y, z)) { "Position $x, $y, $z outside of $bounds" }
        require((bounds.x - x) % 16 == 0 && (bounds.z - z) % 16 == 0 && (bounds.z - z) % 16 == 0) { "Position $x, $y, $z is not a chunk origin" }
        return if (bounds.size <= 16) this.nonAirBlockStates
        else {
            val index = indexOf(x, y, z, 16, 16, 16)
            val n = nodes[index]
            n?.nonAirInChunk(x, y, z) ?: if ((containers[index] ?: default).isAir) 0 else 4096
        }
    }

    companion object {
        private val blocTreePool = getPool<BlocTree>()

        operator fun invoke(
            x: Int, y: Int, z: Int,
            size: Int, default: BlockState
        ): BlocTree {
            return BlocTree(x, y, z, size).apply {
                this.default = default
                this.otPool = blocTreePool
            }
        }
    }
}
