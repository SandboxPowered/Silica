package org.sandboxpowered.silica.client

interface RenderingFactory {
    val priority: Int
    val name: String

    fun createRenderer(silica: SilicaClient): Renderer
}