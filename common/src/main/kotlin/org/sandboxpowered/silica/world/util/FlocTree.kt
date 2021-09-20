package org.sandboxpowered.silica.world.util

import net.mostlyoriginal.api.utils.pooling.ObjectPool
import org.sandboxpowered.silica.state.fluid.FluidState
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
class FlocTree private constructor(x: Int, y: Int, z: Int, size: Int) :
    GenericTree<FlocTree, FluidState>(x, y, z, size) {
    private var nonAirFluidStates = 0

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
        parent: FlocTree,
        default: FluidState
    ): FlocTree {
        super.init(treeDepth, x, y, z, size, parent, default)
        this.nonAirFluidStates = if (default.isAir) 0 else size.toDouble().pow(3).toInt()
        return this
    }

    override fun internalSet(x: Int, y: Int, z: Int, value: FluidState): FluidState {
        val old = super.internalSet(x, y, z, value)

        if (old.isAir && !value.isAir) ++nonAirFluidStates
        else if (!old.isAir && value.isAir) --nonAirFluidStates

        return old
    }

    fun nonAirInChunk(x: Int, y: Int, z: Int): Int {
        require(bounds.contains(x, y, z)) { "Position $x, $y, $z outside of $bounds" }
        require((bounds.x - x) % 16 == 0 && (bounds.z - z) % 16 == 0 && (bounds.z - z) % 16 == 0) { "Position $x, $y, $z is not a chunk origin" }
        return if (bounds.size <= 16) this.nonAirFluidStates
        else {
            val index = indexOf(x, y, z, 16, 16, 16)
            val n = nodes[index]
            n?.nonAirInChunk(x, y, z) ?: if ((containers[index] ?: default).isAir) 0 else 4096
        }
    }

    companion object {
        private val flocTreePool = getPool<FlocTree>()

        operator fun invoke(
            x: Int, y: Int, z: Int,
            size: Int, default: FluidState
        ): FlocTree {
            return FlocTree(x, y, z, size).apply {
                this.default = default
                this.otPool = flocTreePool
            }
        }
    }
}
