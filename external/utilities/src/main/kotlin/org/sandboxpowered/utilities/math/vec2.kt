package org.sandboxpowered.utilities.math

import org.joml.Vector2f
import org.joml.Vector2fc
import org.joml.Vector2i
import org.joml.Vector2ic

val Vector2fc.x: Float
    get() = x()
val Vector2fc.y: Float
    get() = y()

val Vector2ic.x: Int
    get() = x()
val Vector2ic.y: Int
    get() = y()

fun vec2(x: Float, y: Float): Vector2f = Vector2f(x, y)
fun vec2(x: Int, y: Int): Vector2i = Vector2i(x, y)