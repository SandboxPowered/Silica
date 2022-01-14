package org.sandboxpowered.utilities.extensions

import kotlin.math.pow

val Int.isPowerOfTwo: Boolean
    get() = this != 0 && this and this - 1 == 0

val Int.toPowerOfTwo: Int
    get() {
        if (isPowerOfTwo)
            return this
        var sub = this - 1
        for (t in 0..4) sub = sub or (sub shr 2.0.pow(t).toInt())
        return sub + 1
    }