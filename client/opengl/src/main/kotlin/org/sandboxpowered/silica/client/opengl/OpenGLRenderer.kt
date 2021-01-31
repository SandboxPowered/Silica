package org.sandboxpowered.silica.client.opengl

import org.joml.Vector3f
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import org.sandboxpowered.silica.client.*
import org.sandboxpowered.silica.util.getResourceAsString

class OpenGLRenderer(val silica: Silica) : Renderer {
    private val fov = Math.toRadians(60.0).toFloat()

    private val zNear = 0.01f

    private val zFar = 1000f

    private val transforms = Transforms()

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
            -0.5f, 0.5f, 0f,
            -0.5f, -0.5f, 0f,
            0.5f, -0.5f, 0f,
            0.5f, 0.5f, 0f,
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
        shaderProgram.createUniform("projectionMatrix")
        shaderProgram.createUniform("worldMatrix")

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    }

    private var pos = Vector3f(0f, 0f, -2f)
    private var rot = Vector3f()

    override fun frame() {
        window.setTitle("Sandbox Silica ${window.currentFps}")

        if (window.resized) {
            glViewport(0, 0, window.width, window.height)
            window.resized = false
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)

        rot.y += 0.1f

        shaderProgram.bind()
        shaderProgram.setUniform(
            "projectionMatrix",
            transforms.getProjectionMatrix(fov, window.width.toFloat(), window.height.toFloat(), zNear, zFar)
        )
        shaderProgram.setUniform("worldMatrix", transforms.getWorldMatrix(pos, rot, 1f))

        glBindVertexArray(mesh.vaoId)
        glDrawElements(GL_TRIANGLES, mesh.vertexCount, GL_UNSIGNED_INT, 0)

        glBindVertexArray(0)

        shaderProgram.unbind()
    }

    override fun cleanup() {
        shaderProgram.cleanup()

        mesh.cleanup()
    }

    class OpenGLRenderingFactory : RenderingFactory {
        override fun getPriority(): Int = 600
        override fun getId(): String = "opengl"
        override fun createRenderer(silica: Silica): Renderer = OpenGLRenderer(silica)
    }
}
