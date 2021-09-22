package org.sandboxpowered.silica.util.extensions

import kotlin.math.pow

fun Int.pow(i: Int): Int = toDouble().pow(i).toInt()
fun Int.pow(d: Double): Int = toDouble().pow(d).toInt()