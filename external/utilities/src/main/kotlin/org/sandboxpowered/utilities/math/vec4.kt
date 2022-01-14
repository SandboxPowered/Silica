package org.sandboxpowered.utilities.math

import org.joml.*

val Vector4fc.x: Float
    get() = x()
val Vector4fc.y: Float
    get() = y()
val Vector4fc.z: Float
    get() = z()
val Vector4fc.w: Float
    get() = w()

val Vector4ic.x: Int
    get() = x()
val Vector4ic.y: Int
    get() = y()
val Vector4ic.z: Int
    get() = z()
val Vector4ic.w: Int
    get() = w()

fun vec4(x: Float, y: Float, z: Float, w: Float): Vector4f = Vector4f(x, y, z, w)
fun vec4(vec: Vector2f, z: Float, w: Float): Vector4f = Vector4f(vec.x, vec.y, z, w)
fun vec4(vec: Vector3f, w: Float): Vector4f = Vector4f(vec.x, vec.y, vec.z, w)
fun vec4(x: Int, y: Int, z: Int, w: Int): Vector4i = Vector4i(x, y, z, w)
fun vec4(vec: Vector2i, z: Int, w: Int): Vector4i = Vector4i(vec.x, vec.y, z, w)
fun vec4(vec: Vector3i, w: Int): Vector4i = Vector4i(vec.x, vec.y, vec.z, w)