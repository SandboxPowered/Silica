package org.sandboxpowered.silica.api.world

import org.joml.Vector3fc
import org.sandboxpowered.utilities.math.x
import org.sandboxpowered.utilities.math.y
import org.sandboxpowered.utilities.math.z
import kotlin.math.max
import kotlin.math.min

abstract class WorldSelection {
    abstract val x: Int
    abstract val y: Int
    abstract val z: Int
    abstract val width: Int
    abstract val height: Int
    abstract val depth: Int

    val maxX get() = x + width - 1
    val maxY get() = y + height - 1
    val maxZ get() = z + depth - 1

    fun contains(x: Int, y: Int, z: Int): Boolean =
        this.x <= x && this.x + this.width > x
                && this.y <= y && this.y + this.height > y
                && this.z <= z && this.z + this.depth > z

    operator fun contains(other: WorldSelection): Boolean =
        this.x <= other.x && this.x + this.width >= other.maxX
                && this.y <= other.y && this.y + this.height >= other.maxY
                && this.z <= other.z && this.z + this.depth >= other.maxZ

    fun walkX(step: Int): IntProgression = (x..maxX step step)
    fun walkY(step: Int): IntProgression = (y..maxY step step)
    fun walkZ(step: Int): IntProgression = (z..maxZ step step)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is WorldSelection) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (depth != other.depth) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        result = 31 * result + width
        result = 31 * result + height
        result = 31 * result + depth
        return result
    }
}

inline fun WorldSelection.walk(step: Int, body: (x: Int, y: Int, z: Int) -> Unit) {
    for (x in walkX(step)) for (y in walkY(step)) for (z in walkZ(step)) body(x, y, z)
}

@Suppress("DuplicatedCode") // other one is on AABB with floats
inline fun WorldSelection.walkCorners(body: (x: Int, y: Int, z: Int) -> Unit) {
    body(x, y, z)
    body(maxX, y, z)
    body(maxX, y, maxZ)
    body(x, y, maxZ)
    body(x, maxY, z)
    body(maxX, maxY, z)
    body(maxX, maxY, maxZ)
    body(x, maxY, maxZ)
}

operator fun WorldSelection.contains(point: Vector3fc): Boolean =
    this.x <= point.x && this.x + this.width > point.x
            && this.y <= point.y && this.y + this.height > point.y
            && this.z <= point.z && this.z + this.depth > point.z

fun WorldSelection.rayCast(from: Vector3fc, ray: Vector3fc): Float {
    if (from in this) return 0f
    val t1 = if (ray.x == 0f) Float.MIN_VALUE else (x - from.x) / ray.x
    val t2 = if (ray.x == 0f) Float.MAX_VALUE else (x + width - from.x) / ray.x
    val t3 = if (ray.y == 0f) Float.MIN_VALUE else (y - from.y) / ray.y
    val t4 = if (ray.y == 0f) Float.MAX_VALUE else (y + height - from.y) / ray.y
    val t5 = if (ray.z == 0f) Float.MIN_VALUE else (z - from.z) / ray.z
    val t6 = if (ray.z == 0f) Float.MAX_VALUE else (z + depth - from.z) / ray.z

    val tmin = max(max(min(t1, t2), min(t3, t4)), min(t5, t6))
    val tmax = min(min(max(t1, t2), max(t3, t4)), max(t5, t6))

    if (tmax < 0) return -1f
    if (tmin > tmax) return -1f

    return if (tmin >= 0f) tmin
    else error("Should never happen if I understood this right. If you're seeing this error, it means I didn't")
}