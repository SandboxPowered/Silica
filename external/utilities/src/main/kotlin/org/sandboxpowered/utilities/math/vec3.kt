package org.sandboxpowered.utilities.math

import org.joml.*

val Vector3fc.x: Float
    get() = x()
val Vector3fc.y: Float
    get() = y()
val Vector3fc.z: Float
    get() = z()

val Vector3fc.xy: Vector2f
    get() = vec2(x, y)
val Vector3fc.xz: Vector2f
    get() = vec2(x, z)
val Vector3fc.yz: Vector2f
    get() = vec2(y, z)

val Vector3ic.x: Int
    get() = x()
val Vector3ic.y: Int
    get() = y()
val Vector3ic.z: Int
    get() = z()

val Vector3ic.xy: Vector2i
    get() = vec2(x, y)
val Vector3ic.xz: Vector2i
    get() = vec2(x, z)
val Vector3ic.yz: Vector2i
    get() = vec2(y, z)

fun vec3(f: Float): Vector3f = Vector3f(f)
fun vec3(x: Float, y: Float, z: Float): Vector3f = Vector3f(x, y, z)
fun vec3(vec: Vector2f, z: Float): Vector3f = vec3(vec.x, vec.y, z)
fun vec3(i: Int): Vector3i = Vector3i(i)
fun vec3(x: Int, y: Int, z: Int): Vector3i = Vector3i(x, y, z)
fun vec3(vec: Vector2i, z: Int): Vector3i = vec3(vec.x, vec.y, z)

fun Vector3ic.toFloat(): Vector3f = vec3(x.toFloat(), y.toFloat(), z.toFloat())

operator fun Vector3ic.plus(o: Vector3ic): Vector3i = add(o, Vector3i())
operator fun Vector3ic.minus(o: Vector3ic): Vector3i = sub(o, Vector3i())
operator fun Vector3ic.times(o: Vector3ic): Vector3i = mul(o, Vector3i())
operator fun Vector3i.plusAssign(o: Vector3ic) {
    add(o)
}

operator fun Vector3i.minusAssign(o: Vector3ic) {
    sub(o)
}

operator fun Vector3i.timesAssign(o: Vector3ic) {
    mul(o)
}

operator fun Vector3ic.times(o: Int): Vector3i = mul(o, Vector3i())
operator fun Vector3ic.times(o: Float): Vector3f = toFloat().mul(o)

operator fun Vector3fc.plus(o: Vector3fc): Vector3f = add(o, Vector3f())
operator fun Vector3fc.minus(o: Vector3fc): Vector3f = sub(o, Vector3f())
operator fun Vector3fc.times(o: Vector3fc): Vector3f = mul(o, Vector3f())
operator fun Vector3fc.times(o: Float): Vector3f = mul(o, Vector3f())
operator fun Vector3f.plusAssign(o: Vector3fc) {
    add(o)
}

operator fun Vector3f.minusAssign(o: Vector3fc) {
    sub(o)
}

operator fun Vector3f.timesAssign(o: Vector3fc) {
    mul(o)
}