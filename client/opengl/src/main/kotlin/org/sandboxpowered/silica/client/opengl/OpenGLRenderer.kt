package org.sandboxpowered.silica.client.opengl

import com.sun.source.tree.BlockTree
import it.unimi.dsi.fastutil.floats.FloatArrayList
import it.unimi.dsi.fastutil.floats.FloatArraySet
import it.unimi.dsi.fastutil.ints.IntArrayList
import org.joml.Vector3f
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import org.sandboxpowered.silica.client.*
import org.sandboxpowered.silica.util.getResourceAsString
import org.sandboxpowered.silica.world.util.iterateCube

class OpenGLRenderer(private val silica: Silica) : Renderer {
    private val fov = Math.toRadians(60.0).toFloat()

    private val zNear = 0.01f

    private val zFar = 1000f

    private val transforms = Transforms()

    private lateinit var shaderProgram: ShaderProgram
    private lateinit var mesh: Mesh
    private val window: Window
        get() = silica.window

    override fun getName(): String = "OpenGL"

    override fun init() {
        GL.createCapabilities()

        val positions = floatArrayOf(
            // VO
            0.0f,  1.0f,  1.0f,
            // V1
            0.0f, 0.0f,  1.0f,
            // V2
            1.0f, 0.0f,  1.0f,
            // V3
            1.0f,  1.0f,  1.0f,
            // V4
            0.0f,  1.0f, 0.0f,
            // V5
            1.0f,  1.0f, 0.0f,
            // V6
            0.0f, 0.0f, 0.0f,
            // V7
            1.0f, 0.0f, 0.0f,
        )
        val colours = floatArrayOf(
            0.5f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f,
            0.0f, 0.0f, 0.5f,
            0.0f, 0.5f, 0.5f,
            0.5f, 0.0f, 0.0f,
            0.0f, 0.5f, 0.0f,
            0.0f, 0.0f, 0.5f,
            0.0f, 0.5f, 0.5f,
        )
        val indices = intArrayOf(
            // Front face
            0, 1, 3, 3, 1, 2,
            // Back face
            7, 6, 4, 7, 4, 5,
            // Left face
            6, 1, 0, 6, 0, 4,
            // Right face
            3, 2, 7, 5, 3, 7,
            // Top Face
            4, 0, 3, 5, 4, 3,
            // Bottom face
            2, 1, 6, 2, 6, 7,
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
        if (window.resized) {
            glViewport(0, 0, window.width, window.height)
            window.resized = false
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        GL11.glEnable(GL_DEPTH_TEST)

        rot.y += 0.1f
        rot.x += 0.1f

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
