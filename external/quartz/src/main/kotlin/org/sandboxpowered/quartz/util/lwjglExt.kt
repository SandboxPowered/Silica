package org.sandboxpowered.quartz.util

import org.lwjgl.system.MemoryStack
import java.nio.IntBuffer
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@OptIn(ExperimentalContracts::class)
inline fun <R> stackPush(block: (MemoryStack) -> R): R {
    contract {
        callsInPlace(block, InvocationKind.EXACTLY_ONCE)
    }
    return MemoryStack.stackPush().use(block)
}

fun MemoryStack.ints(array: IntArray): IntBuffer = ints(*array)