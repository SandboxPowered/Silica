package org.sandboxpowered.silica.client.vulkan

import org.lwjgl.system.MemoryStack.stackGet
import java.nio.LongBuffer


class Frame(val imageAvailableSemaphore: Long, val renderFinishedSemaphore: Long, val fence: Long) {
    fun pImageAvailableSemaphore(): LongBuffer = stackGet().longs(imageAvailableSemaphore)
    fun pRenderFinishedSemaphore(): LongBuffer = stackGet().longs(renderFinishedSemaphore)
    fun pFence(): LongBuffer = stackGet().longs(fence)
}