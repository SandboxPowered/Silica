package org.sandboxpowered.silica.api.math

import org.joml.Matrix4fStack
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <R> Matrix4fStack.push(block: (Matrix4fStack) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    this.pushMatrix()
    val r = block(this)
    this.popMatrix()
    return r
}