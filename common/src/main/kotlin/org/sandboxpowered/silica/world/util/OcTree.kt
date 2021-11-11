package org.sandboxpowered.silica.world.util

import com.artemis.utils.Bag
import com.artemis.utils.IntBag
import net.mostlyoriginal.api.utils.QuadTree
import net.mostlyoriginal.api.utils.pooling.ObjectPool
import net.mostlyoriginal.api.utils.pooling.Poolable
import org.sandboxpowered.silica.api.util.extensions.bag
import org.sandboxpowered.silica.api.util.extensions.getPool
import org.sandboxpowered.silica.api.util.extensions.plusAssign

/**
 * Octree for optimized queries in 3d space
 *
 * @see QuadTree for the original 2d implementation
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
@Suppress("MemberVisibilityCanBePrivate", "unused")
class OcTree @JvmOverloads constructor(
    x: Float, y: Float, z: Float,
    width: Float, height: Float, depth: Float,
    capacity: Int = 16, private var maxDepth: Int = 8
) : Poolable {
    private var parent: OcTree? = null
    private var nextFlag = 1L
    private var idToContainer: Bag<Container> = parent?.idToContainer ?: bag()
    var bounds = Container().set(x, y, z, width, height, depth)
        private set
    var nodes = Array<OcTree?>(8) { null }
        private set
    private var maxInBucket = capacity
    private var containers = bag<Container>(maxInBucket)
    private var treeDepth = 0

    @Deprecated("for [ObjectPool] use only", level = DeprecationLevel.HIDDEN)
    internal constructor() : this(0f, 0f, 0f, 0f, 0f, 0f)

    private fun init(
        treeDepth: Int,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float,
        parent: OcTree?
    ): OcTree {
        this.treeDepth = treeDepth
        this.bounds.set(x, y, z, width, height, depth)
        this.parent = parent
        this.idToContainer = parent?.idToContainer ?: bag()

        return this
    }

    private fun indexOf(
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float
    ): Int {
        val midX = bounds.x + bounds.width / 2
        val midY = bounds.y + bounds.height / 2
        val midZ = bounds.z + bounds.depth / 2

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
     * Returns a unique flag for inserting and querying this quad tree.
     *
     * Using this flag allows for querying only for entities inserted with the same flag set.
     */
    fun nextFlag(): Long {
        val flag = nextFlag
        nextFlag = nextFlag shl 1

        return flag
    }

    /**
     * Upserts the given entity id to the tree with the bounds, updating its flags and position if it's already been added, inserting it otherwise.
     *
     * Use [update] instead if you know the entity is already contained.
     */
    fun upsert(
        eid: Int, flags: Long = 0L,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float
    ) {
        val c = if (eid < idToContainer.size()) idToContainer[eid] else null
        if (c != null && c.eid != -1) {
            c.flags = c.flags or flags
            update(eid, x, y, z, width, height, depth)
            return
        }

        insert(eid, flags, x, y, z, width, height, depth)
    }

    /**
     * Upserts the given entity id to the tree with the bounds, updating its flags and position if it's already been added, inserting it otherwise.
     *
     * Use [update] instead if you know the entity is already contained.
     */
    fun upsert(
        eid: Int,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float
    ) = upsert(eid, 0L, x, y, z, width, height, depth)

    /**
     * Inserts given entity id to tree with given bounds
     */
    fun insert(
        eid: Int,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float
    ) = this.insert(cPool.obtain().set(eid, 0L, x, y, z, width, height, depth))

    /**
     * Inserts given entity id to tree with given bounds
     */
    fun insert(
        eid: Int, flags: Long = 0L,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float
    ) = this.insert(cPool.obtain().set(eid, flags, x, y, z, width, height, depth))

    private fun insert(c: Container) {
        if (nodes[0] != null) {
            val index = indexOf(c.x, c.y, c.z, c.width, c.height, c.depth)
            if (index != OUTSIDE) {
                nodes[index]!!.insert(c)
                return
            }
        }
        c.parent = this
        idToContainer[c.eid] = c
        containers.add(c)

        if (containers.size() > maxInBucket && treeDepth < maxDepth) {
            if (nodes[0] == null) {
                val halfWidth = bounds.width / 2
                val halfHeight = bounds.height / 2
                val halfDepth = bounds.depth / 2
                nodes[DNW] = otPool.obtain().init(
                    treeDepth + 1,
                    bounds.x, bounds.y, bounds.z,
                    halfWidth, halfHeight, halfDepth,
                    this
                )
                nodes[DNE] = otPool.obtain().init(
                    treeDepth + 1,
                    bounds.x + halfWidth, bounds.y, bounds.z,
                    halfWidth, halfHeight, halfDepth,
                    this
                )
                nodes[DSW] = otPool.obtain().init(
                    treeDepth + 1,
                    bounds.x, bounds.y, bounds.z + halfDepth,
                    halfWidth, halfHeight, halfDepth,
                    this
                )
                nodes[DSE] = otPool.obtain().init(
                    treeDepth + 1,
                    bounds.x + halfWidth, bounds.y, bounds.z + halfDepth,
                    halfWidth, halfHeight, halfDepth,
                    this
                )


                nodes[UNW] = otPool.obtain().init(
                    treeDepth + 1,
                    bounds.x, bounds.y + halfHeight, bounds.z,
                    halfWidth, halfHeight, halfDepth,
                    this
                )
                nodes[UNE] = otPool.obtain().init(
                    treeDepth + 1,
                    bounds.x + halfWidth, bounds.y + halfHeight, bounds.z,
                    halfWidth, halfHeight, halfDepth,
                    this
                )
                nodes[USW] = otPool.obtain().init(
                    treeDepth + 1,
                    bounds.x, bounds.y + halfHeight, bounds.z + halfDepth,
                    halfWidth, halfHeight, halfDepth,
                    this
                )
                nodes[USE] = otPool.obtain().init(
                    treeDepth + 1,
                    bounds.x + halfWidth, bounds.y + halfHeight, bounds.z + halfDepth,
                    halfWidth, halfHeight, halfDepth,
                    this
                )
            }
            val items = containers.data
            for (i in containers.size() - 1 downTo 0) {
                val next = items[i]
                val index = indexOf(next.x, next.y, next.z, next.width, next.height, next.depth)
                if (index != OUTSIDE) {
                    nodes[index]!!.insert(next)
                    containers.remove(i)
                }
            }
        }
    }


    /**
     * Returns entity ids of entities that are inside [OcTree]s that contain given point
     *
     * Returned entities must be filtered further as these results are not exact
     */
    operator fun get(fill: IntBag, x: Float, y: Float, z: Float): IntBag {
        if (bounds.contains(x, y, z)) {
            if (nodes[0] != null) {
                val index = indexOf(x, y, z, 0f, 0f, 0f)
                if (index != OUTSIDE) nodes[index]!![fill, x, y, z, 0f, 0f, 0f]
            }
            for (i in 0 until containers.size()) fill += containers[i].eid
        }
        return fill
    }

    /**
     * Returns entity ids of entities that are inside [OcTree]s that contain given point and have the given flags set
     *
     *
     * Returned entities must be filtered further as these results are not exact
     *
     * @see nextFlag
     */
    operator fun get(fill: IntBag, x: Float, y: Float, z: Float, flags: Long): IntBag {
        if (flags == 0L) return this[fill, x, y, z]

        if (bounds.contains(x, y, z)) {
            if (nodes[0] != null) {
                val index = indexOf(x, y, z, 0f, 0f, 0f)
                if (index != OUTSIDE) nodes[index]!![fill, x, y, z, 0f, 0f, 0f, flags]
            }
            for (i in 0 until containers.size()) {
                val c = containers[i]
                if (c.flags and flags > 0) fill += c.eid
            }
        }
        return fill
    }

    /**
     * Returns entity ids of entities that bounds contain given point
     */
    fun getExact(fill: IntBag, x: Float, y: Float, z: Float): IntBag {
        if (bounds.contains(x, y, z)) {
            if (nodes[0] != null) {
                val index = indexOf(x, y, z, 0f, 0f, 0f)
                if (index != OUTSIDE) nodes[index]!!.getExact(fill, x, y, z, 0f, 0f, 0f)
            }
            for (i in 0 until containers.size()) {
                val c = containers[i]
                if (c.contains(x, y, z)) fill += c.eid
            }
        }
        return fill
    }

    /**
     * Returns entity ids of entities that bounds contain given point and have the given flags set
     *
     * @see nextFlag
     */
    fun getExact(fill: IntBag, x: Float, y: Float, z: Float, flags: Long): IntBag {
        if (flags == 0L) return getExact(fill, x, y, z)

        if (bounds.contains(x, y, z)) {
            if (nodes[0] != null) {
                val index = indexOf(x, y, z, 0f, 0f, 0f)
                if (index != OUTSIDE) nodes[index]!!.getExact(fill, x, y, z, 0f, 0f, 0f, flags)
            }
            for (i in 0 until containers.size()) {
                val c = containers[i]
                if (c.flags and flags > 0 && c.contains(x, y, z)) fill += c.eid
            }
        }
        return fill
    }

    /**
     * Returns entity ids of entities that are inside [OcTree]s that overlap given bounds
     *
     * Returned entities must be filtered further as these results are not exact
     */
    operator fun get(
        fill: IntBag,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float
    ): IntBag {
        if (bounds.overlaps(x, y, z, width, height, depth)) {
            if (nodes[0] != null) {
                val index = indexOf(x, y, z, width, height, depth)
                if (index != OUTSIDE) nodes[index]!![fill, x, y, z, width, height, depth]
                else for (i in nodes.indices) nodes[i]!![fill, x, y, z, width, height, depth]
            }
            for (i in 0 until containers.size()) {
                val c = containers[i]
                fill += c.eid
            }
        }
        return fill
    }

    /**
     * Returns entity ids of entities that are inside [OcTree]s that overlap given bounds and have the given flags set
     *
     *
     * Returned entities must be filtered further as these results are not exact
     *
     * @see nextFlag
     */
    operator fun get(
        fill: IntBag,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float,
        flags: Long
    ): IntBag {
        if (flags == 0L) return this[fill, x, y, z, width, height, depth]

        if (bounds.overlaps(x, y, z, width, height, depth)) {
            if (nodes[0] != null) {
                val index = indexOf(x, y, z, width, height, depth)
                if (index != OUTSIDE) nodes[index]!![fill, x, y, z, width, height, depth, flags]
                else for (i in nodes.indices) nodes[i]!![fill, x, y, z, width, height, depth, flags]
            }
            for (i in 0 until containers.size()) {
                val c = containers[i]
                if (c.flags and flags > 0) fill += c.eid
            }
        }
        return fill
    }

    /**
     * Returns entity ids of entities that overlap given bounds
     */
    fun getExact(
        fill: IntBag,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float
    ): IntBag {
        if (bounds.overlaps(x, y, z, width, height, depth)) {
            if (nodes[0] != null) {
                val index = indexOf(x, y, z, width, height, depth)
                if (index != OUTSIDE) nodes[index]!!.getExact(fill, x, y, z, width, height, depth)
                else for (i in nodes.indices) nodes[i]!!.getExact(fill, x, y, z, width, height, depth)
            }
            for (i in 0 until containers.size()) {
                val c = containers[i]
                if (c.overlaps(x, y, z, width, height, depth)) {
                    fill += c.eid
                }
            }
        }
        return fill
    }

    /**
     * Returns entity ids of entities that overlap given bounds and have the given flags set
     *
     * @see nextFlag
     */
    fun getExact(
        fill: IntBag,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float,
        flags: Long
    ): IntBag {
        if (flags == 0L) {
            return getExact(fill, x, y, z, width, height, depth)
        }
        if (bounds.overlaps(x, y, z, width, height, depth)) {
            if (nodes[0] != null) {
                val index = indexOf(x, y, z, width, height, depth)
                if (index != OUTSIDE) {
                    nodes[index]!!.getExact(fill, x, y, z, width, height, depth, flags)
                } else {
                    // if test bounds don't fully fit inside a node, we need to check them all
                    for (i in nodes.indices) {
                        nodes[i]!!.getExact(fill, x, y, z, width, height, depth, flags)
                    }
                }
            }
            for (i in 0 until containers.size()) {
                val c = containers[i]
                if (c.flags and flags > 0 && c.overlaps(x, y, z, width, height, depth)) {
                    fill += c.eid
                }
            }
        }
        return fill
    }

    /**
     * Update position for this id with new one
     */
    fun update(
        id: Int,
        x: Float, y: Float, z: Float,
        width: Float, height: Float, depth: Float
    ) {
        val c = idToContainer[id]
        c.set(id, c.flags, x, y, z, width, height, depth)

        var qTree = c.parent!!
        if (!qTree.bounds.contains(c)) {
            qTree.containers.remove(c)
            while (qTree.parent != null && !qTree.bounds.contains(c)) qTree = qTree.parent!!
            qTree.insert(c)
        }
    }

    /**
     * Remove given id from the tree
     */
    fun remove(id: Int) {
        val c = idToContainer[id] ?: return
        if (c.parent != null) c.parent!!.containers.remove(c)
        idToContainer[id] = null
        cPool.free(c)
    }

    /**
     * Reset the OcTree by removing all nodes and stored ids
     */
    override fun reset() {
        for (i in containers.size() - 1 downTo 0) cPool.free(containers.remove(i))
        for (i in nodes.indices) if (nodes[i] != null) {
            otPool.free(nodes[i])
            nodes[i] = null
        }
    }

    /**
     * Dispose of the OcTree by removing all nodes and stored ids
     */
    fun dispose() = reset()

    override fun toString() = "OcTree(treeDepth=$treeDepth)"

    /**
     * Simple container for entity ids and their bounds
     */
    class Container : Poolable {
        var eid = 0
            private set
        var flags: Long = 0
        var x = 0f
            private set
        var y = 0f
            private set
        var z = 0f
            private set
        var width = 0f
            private set
        var height = 0f
            private set
        var depth = 0f
            private set
        var parent: OcTree? = null

        fun set(
            eid: Int, flags: Long,
            x: Float, y: Float, z: Float,
            width: Float, height: Float, depth: Float
        ): Container {
            this.eid = eid
            this.flags = flags
            this.x = x
            this.y = y
            this.z = z
            this.width = width
            this.height = height
            this.depth = depth
            return this
        }

        fun set(
            x: Float, y: Float, z: Float,
            width: Float, height: Float, depth: Float
        ): Container {
            this.x = x
            this.y = y
            this.z = z
            this.width = width
            this.height = height
            this.depth = depth
            return this
        }

        fun contains(x: Float, y: Float, z: Float): Boolean =
            this.x <= x && this.x + this.width >= x
                    && this.y <= y && this.y + this.height >= y
                    && this.z <= z && this.z + this.depth >= z

        fun overlaps(
            x: Float, y: Float, z: Float,
            width: Float, height: Float, depth: Float
        ): Boolean = this.x < x + width && this.x + this.width >= x
                && this.y < y + height && this.y + this.height >= y
                && this.z < z + depth && this.z + this.depth >= z

        fun contains(
            ox: Float, oy: Float, oz: Float,
            oWidth: Float, oHeight: Float, oDepth: Float
        ): Boolean {
            val xMax = ox + oWidth
            val yMax = oy + oHeight
            val zMax = oz + oDepth

            return ox > x && ox < x + width && xMax > x && xMax < x + width
                    && oy > y && oy < y + height && yMax > y && yMax < y + height
                    && oz > z && oz < z + depth && zMax > z && yMax < z + depth
        }

        operator fun contains(c: Container): Boolean = contains(c.x, c.y, c.z, c.width, c.height, c.depth)

        override fun reset() {
            eid = -1
            flags = 0L
            x = 0f
            y = 0f
            z = 0f
            width = 0f
            height = 0f
            depth = 0f
            parent = null
        }
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

        private val otPool: ObjectPool<OcTree> = getPool()
        private val cPool: ObjectPool<Container> = getPool()
    }
}