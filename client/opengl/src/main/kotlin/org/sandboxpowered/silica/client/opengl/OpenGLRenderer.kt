package org.sandboxpowered.silica.client.opengl

import com.github.zafarkhaja.semver.Version
import de.matthiasmann.twl.utils.PNGDecoder
import org.joml.Vector3f
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import org.sandboxpowered.silica.client.*
import org.sandboxpowered.silica.resources.ResourceType
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.extensions.getResourceAsString
import java.nio.ByteBuffer

class OpenGLRenderer(private val silica: Silica) : Renderer {

    private val window: Window
        get() = silica.window

    override val name: String = "OpenGL"
    override val version: Version = Version.forIntegers(0, 1, 0)

    private val fov = Math.toRadians(60.0).toFloat()

    private val zNear = 0.01f

    private val zFar = 1000f

    private lateinit var shaderProgram: ShaderProgram
    private lateinit var mesh: Mesh
    private val transforms = Transforms()
    private var textureId = -1

    override fun init() {
        GL.createCapabilities()

        val positions = floatArrayOf(
            // VO
            0.0f, 1.0f, 1.0f,
            // V1
            0.0f, 0.0f, 1.0f,
            // V2
            1.0f, 0.0f, 1.0f,
            // V3
            1.0f, 1.0f, 1.0f,
            // V4
            0.0f, 1.0f, 0.0f,
            // V5
            1.0f, 1.0f, 0.0f,
            // V6
            0.0f, 0.0f, 0.0f,
            // V7
            1.0f, 0.0f, 0.0f,
        )
        val texCoords = floatArrayOf(
            0.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
            1.0f, 0.0f,
            1.0f, 1.0f,
            0.0f, 1.0f,
            0.0f, 0.0f,
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
        mesh = Mesh(positions, texCoords, indices)

        shaderProgram = ShaderProgram()
        shaderProgram.createVertexShader(javaClass.getResourceAsString("/assets/silica/shaders/vertex.glsl"))
        shaderProgram.createFragmentShader(javaClass.getResourceAsString("/assets/silica/shaders/fragment.glsl"))
        shaderProgram.link()
        shaderProgram.createUniform("projectionMatrix")
        shaderProgram.createUniform("worldMatrix")
        shaderProgram.createUniform("texture_sampler")

        val decoder =
            PNGDecoder(silica.assetManager.open(ResourceType.ASSETS, Identifier.of("textures/block/stone.png")))
        val buf = ByteBuffer.allocateDirect(4 * decoder.width * decoder.height)
        decoder.decode(buf, decoder.width * 4, PNGDecoder.Format.RGBA)
        buf.flip()

        textureId = glGenTextures()
        glBindTexture(GL_TEXTURE_2D, textureId)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        GL11.glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, decoder.width, decoder.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buf)

        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glGenerateMipmap(GL_TEXTURE_2D);


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
        shaderProgram.setUniform("texture_sampler", 0)
        glActiveTexture(GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, textureId)

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
        override val priority: Int = 600
        override val name: String = "opengl"

        override fun createRenderer(silica: Silica): Renderer = OpenGLRenderer(silica)
    }
}
