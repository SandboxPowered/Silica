package org.sandboxpowered.silica.client.opengl

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import org.sandboxpowered.silica.client.*
import org.sandboxpowered.silica.util.getResourceAsString


class OpenGLRenderer(val silica: Silica) : Renderer {
    private lateinit var shaderProgram: ShaderProgram
    private lateinit var mesh: Mesh
    private val window: Window
        get() {
            return silica.window
        }

    override fun getName(): String = "OpenGL"

    override fun init() {
        GL.createCapabilities()

        val positions = floatArrayOf(
            -0.5f, 0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
            0.5f, -0.5f, 0.0f,
            0.5f, 0.5f, 0.0f
        )
        val colours = floatArrayOf(
            0.5f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f,
            0.0f, 0.0f, 0.5f,
            0.0f, 0.5f, 0.5f
        )
        val indices = intArrayOf(
            0, 1, 3, 3, 1, 2
        )
        mesh = Mesh(positions, colours, indices)

        shaderProgram = ShaderProgram()
        shaderProgram.createVertexShader(javaClass.getResourceAsString("/assets/silica/shaders/vertex.glsl"))
        shaderProgram.createFragmentShader(javaClass.getResourceAsString("/assets/silica/shaders/fragment.glsl"))
        shaderProgram.link()

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    }

    override fun frame() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        if (window.resized) {
            glViewport(0, 0, window.width, window.height)
            window.resized = false
        }

        shaderProgram.bind()

        glBindVertexArray(mesh.vaoId)
        glDrawElements(GL_TRIANGLES, mesh.vertexCount, GL_UNSIGNED_INT, 0);

        glBindVertexArray(0)

        shaderProgram.unbind()
    }

    override fun cleanup() {
        shaderProgram.cleanup()

        mesh.cleanup()
    }
}
