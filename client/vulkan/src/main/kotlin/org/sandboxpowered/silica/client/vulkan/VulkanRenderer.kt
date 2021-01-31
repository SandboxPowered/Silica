package org.sandboxpowered.silica.client.vulkan

import org.sandboxpowered.silica.client.Renderer
import org.sandboxpowered.silica.client.RenderingFactory
import org.sandboxpowered.silica.client.Silica

class VulkanRenderer(val silica: Silica) : Renderer {
    override fun getName(): String = "Vulkan"

    class VulkanRenderingFactory : RenderingFactory {
        override fun getPriority(): Int = 600
        override fun getId(): String = "vulkan"
        override fun createRenderer(silica: Silica): Renderer = VulkanRenderer(silica)
    }
}