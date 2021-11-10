package org.sandboxpowered.silica.api.util.extensions

import org.joml.*
import kotlin.math.cos
import kotlin.math.sin

operator fun Vector2ic.component1() = x()
operator fun Vector2ic.component2() = y()

operator fun Vector2dc.component1() = x()
operator fun Vector2dc.component2() = y()

operator fun Vector2fc.component1() = x()
operator fun Vector2fc.component2() = y()

operator fun Vector3dc.component1() = x()
operator fun Vector3dc.component2() = y()
operator fun Vector3dc.component3() = z()

operator fun Vector3fc.component1() = x()
operator fun Vector3fc.component2() = y()
operator fun Vector3fc.component3() = z()

operator fun Vector3ic.component1() = x()
operator fun Vector3ic.component2() = y()
operator fun Vector3ic.component3() = z()

operator fun Vector2fc.minus(other: Vector2fc): Vector2fc = sub(other, Vector2f())
operator fun Vector2fc.plus(other: Vector2fc): Vector2fc = add(other, Vector2f())

operator fun Vector2ic.minus(other: Vector2ic): Vector2ic = sub(other, Vector2i())
operator fun Vector2ic.plus(other: Vector2ic): Vector2ic = add(other, Vector2i())

operator fun Vector2dc.minus(other: Vector2dc): Vector2dc = sub(other, Vector2d())
operator fun Vector2dc.plus(other: Vector2dc): Vector2dc = add(other, Vector2d())

operator fun Vector3fc.minus(other: Vector3fc): Vector3fc = sub(other, Vector3f())
operator fun Vector3fc.plus(other: Vector3fc): Vector3fc = add(other, Vector3f())

operator fun Vector3ic.minus(other: Vector3ic): Vector3ic = sub(other, Vector3i())
operator fun Vector3ic.plus(other: Vector3ic): Vector3ic = add(other, Vector3i())

operator fun Vector3dc.minus(other: Vector3dc): Vector3dc = sub(other, Vector3d())
operator fun Vector3dc.plus(other: Vector3dc): Vector3dc = add(other, Vector3d())

fun getQuaternionAngle(vec: Vector3f, angle: Float, scale: Boolean): Quaternionf {
    val modifiedAngle = if (scale) angle * 0.017453292f else angle

    val sin = sin(modifiedAngle / 2f)
    return Quaternionf(vec.x() * sin, vec.y() * sin, vec.z() * sin, cos(modifiedAngle / 2f))
}

fun Vector4f.mulComponents(scale: Vector3f) {
    x *= scale.x
    y *= scale.y
    z *= scale.z
}

fun Matrix4f.read(quat: Quaternionf): Matrix4f {
    val x = quat.x()
    val y = quat.y()
    val z = quat.z()
    val w = quat.w()
    val x2 = 2f * x * x
    val y2 = 2f * y * y
    val z2 = 2f * z * z

    m00(1f - y2 - z2)
    m11(1f - z2 - x2)
    m22(1f - x2 - y2)
    m33(1f)
    val xy = x * y
    val yz = y * z
    val zx = z * x
    val xw = x * w
    val yw = y * w
    val zw = z * w
    m10(2f * (xy + zw))
    m01(2f * (xy - zw))
    m20(2f * (zx - yw))
    m02(2f * (zx + yw))
    m21(2f * (yz + xw))
    m12(2f * (yz - xw))
    return this
}