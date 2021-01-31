package org.sandboxpowered.silica.client

interface RenderingFactory {
    fun getPriority(): Int

    fun createRenderer(silica: Silica): Renderer

    fun getId(): String
}