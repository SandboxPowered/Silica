package org.sandboxpowered.silica.client.vulkan

import org.apache.commons.io.FileUtils.sizeOf
import org.joml.Vector2fc
import org.joml.Vector3fc
import org.lwjgl.system.MemoryStack
import org.lwjgl.vulkan.VK10.*
import org.lwjgl.vulkan.VkVertexInputAttributeDescription
import org.lwjgl.vulkan.VkVertexInputBindingDescription

class Vertex(val pos: Vector2fc, val color: Vector3fc) {

    companion object {
        const val sizeOf = (2 + 3) * Float.SIZE_BYTES
        private const val posOffset = 0
        private const val colorOffset = 2 * Float.SIZE_BYTES

        fun getBindingDescription(stack: MemoryStack): VkVertexInputBindingDescription.Buffer {
            val binding = VkVertexInputBindingDescription.callocStack(1, stack)
            binding.binding(0)
            binding.stride(sizeOf)
            binding.inputRate(VK_VERTEX_INPUT_RATE_VERTEX)
            return binding
        }

        fun getAttributeDescriptions(stack: MemoryStack): VkVertexInputAttributeDescription.Buffer {
            val attributeDescription = VkVertexInputAttributeDescription.callocStack(2, stack)

            attributeDescription.apply(0) {
                it.binding(0)
                it.location(0)
                it.format(VK_FORMAT_R32G32_SFLOAT)
                it.offset(posOffset)
            }
            attributeDescription.apply(1) {
                it.binding(0)
                it.location(1)
                it.format(VK_FORMAT_R32G32B32_SFLOAT)
                it.offset(colorOffset)
            }
            return attributeDescription.rewind()
        }
    }
}