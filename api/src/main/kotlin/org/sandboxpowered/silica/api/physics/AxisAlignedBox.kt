package org.sandboxpowered.silica.api.physics

import org.joml.Vector3fc
import org.sandboxpowered.silica.api.util.math.Position

class AxisAlignedBox(
    val x: Float,
    val y: Float,
    val z: Float,
    val w: Float,
    val h: Float,
    val d: Float
) {
    companion object {
        val FULL_BLOCK = AxisAlignedBox(0f, 0f, 0f, 1f, 1f, 1f)
    }

    val maxX get() = x + w
    val maxY get() = y + h
    val maxZ get() = z + d

    fun contains(x: Float, y: Float, z: Float): Boolean =
        x >= this.x && x <= this.x + this.w && y >= this.y && y <= this.y + this.h && z >= this.z && z <= this.z + this.d

    fun contains(vector: Vector3fc): Boolean =
        contains(vector.x(), vector.y(), vector.z())

    fun contains(box: AxisAlignedBox): Boolean =
        box.x >= this.x && box.x + box.w <= this.x + this.w && box.y >= this.y && box.y + box.h <= this.y + this.h && box.z >= this.z && box.z + box.d <= this.z + this.d

    fun intersects(box: AxisAlignedBox): Boolean =
        box.x + box.w >= this.x && box.x <= this.x + this.w && box.y + box.h >= this.y && box.y <= this.y + this.h && box.z + box.d >= this.z && box.z <= this.z + this.d

    fun offset(x: Float, y: Float, z: Float): AxisAlignedBox =
        AxisAlignedBox(this.x + x, this.y + y, this.z + z, this.w, this.h, this.d)

    fun offset(x: Int, y: Int, z: Int): AxisAlignedBox =
        AxisAlignedBox(this.x + x, this.y + y, this.z + z, this.w, this.h, this.d)

    fun offset(vector: Vector3fc): AxisAlignedBox = offset(vector.x(), vector.y(), vector.z())

    fun offset(position: Position): AxisAlignedBox = offset(position.x, position.y, position.z)
}

@Suppress("DuplicatedCode") // other one is on WorldSelection with Ints
inline fun AxisAlignedBox.walkCorners(body: (x: Float, y: Float, z: Float) -> Unit) {
    body(x, y, z)
    body(maxX, y, z)
    body(maxX, y, maxZ)
    body(x, y, maxZ)
    body(x, maxY, z)
    body(maxX, maxY, z)
    body(maxX, maxY, maxZ)
    body(x, maxY, maxZ)
}