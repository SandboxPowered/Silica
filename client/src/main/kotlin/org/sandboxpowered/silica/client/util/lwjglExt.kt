package org.sandboxpowered.silica.client.util

import org.lwjgl.system.MemoryStack
import org.sandboxpowered.silica.util.set
import java.nio.IntBuffer

fun MemoryStack.ints(array: IntArray): IntBuffer {
    val buffer = mallocInt(array.size)
    for ((index, i) in array.withIndex()) {
        buffer[index] = i
    }
    return buffer
}
