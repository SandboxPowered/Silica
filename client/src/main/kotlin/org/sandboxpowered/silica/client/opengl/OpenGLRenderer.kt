package org.sandboxpowered.silica.client.opengl

import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15.GL_FLOAT
import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack.stackPush
import org.sandboxpowered.silica.client.Renderer
import org.sandboxpowered.silica.client.ShaderProgram
import org.sandboxpowered.silica.client.Silica
import org.sandboxpowered.silica.client.Window
import org.sandboxpowered.silica.util.getResourceAsString

class OpenGLRenderer(val silica: Silica) : Renderer {
    private lateinit var shaderProgram: ShaderProgram
    private var vboId: Int = -1
    private var vaoId: Int = -1
    var vertices = floatArrayOf(
        0.0f, 0.5f, 0.0f,
        -0.5f, -0.5f, 0.0f,
        0.5f, -0.5f, 0.0f
    )
    private val window: Window
        get() {
            return silica.window
        }

    override fun getName(): String = "OpenGL"

    override fun init() {
        GL.createCapabilities()

        shaderProgram = ShaderProgram()
        shaderProgram.createVertexShader(javaClass.getResourceAsString("/assets/silica/shaders/vertex.glsl"))
        shaderProgram.createFragmentShader(javaClass.getResourceAsString("/assets/silica/shaders/fragment.glsl"))
        shaderProgram.link()

        stackPush().use {
            val verticesBuffer = it.mallocFloat(vertices.size)
            verticesBuffer.put(vertices).flip()
            vaoId = glGenVertexArrays()
            glBindVertexArray(vaoId)
            vboId = glGenBuffers()
            glBindBuffer(GL_ARRAY_BUFFER, vboId)
            glBufferData(GL_ARRAY_BUFFER, verticesBuffer, GL_STATIC_DRAW)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)
            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)
        }

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    }

    override fun frame() {
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        if (window.resized) {
            glViewport(0, 0, window.width, window.height)
            window.resized = false
        }

        shaderProgram.bind()

        // Bind to the VAO
        glBindVertexArray(vaoId)
        glEnableVertexAttribArray(0)

        // Draw the vertices
        glDrawArrays(GL_TRIANGLES, 0, 3)

        // Restore state
        glDisableVertexAttribArray(0)
        glBindVertexArray(0)

        shaderProgram.unbind()
    }

    override fun cleanup() {
        shaderProgram.cleanup()

        glDisableVertexAttribArray(0)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glDeleteBuffers(vboId)

        // Delete the VAO
        glBindVertexArray(0)
        glDeleteVertexArrays(vaoId)
    }
}
