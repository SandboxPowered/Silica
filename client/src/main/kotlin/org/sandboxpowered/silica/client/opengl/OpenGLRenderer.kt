package org.sandboxpowered.silica.client.opengl

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.sandboxpowered.silica.client.Renderer

class OpenGLRenderer : Renderer {
    override fun getName(): String = "OpenGL"

    override fun init() {
        GL.createCapabilities()
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    }

    override fun frame() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
    }
}