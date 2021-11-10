package org.sandboxpowered.silica.client.util

import org.lwjgl.system.MemoryStack
import org.sandboxpowered.silica.api.util.extensions.set
import java.nio.IntBuffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

fun MemoryStack.ints(array: IntArray): IntBuffer {
    val buffer = mallocInt(array.size)
    for ((index, i) in array.withIndex()) {
        buffer[index] = i
    }
    return buffer
}

@OptIn(ExperimentalContracts::class)
inline fun <R> stackPush(block: (MemoryStack) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return MemoryStack.stackPush().use(block)
}