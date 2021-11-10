package org.sandboxpowered.silica.client.opengl

import com.github.zafarkhaja.semver.Version
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import org.sandboxpowered.silica.client.Renderer
import org.sandboxpowered.silica.client.RenderingFactory
import org.sandboxpowered.silica.client.SilicaClient
import org.sandboxpowered.silica.client.Window
import org.sandboxpowered.silica.client.model.BlockModelFormat
import org.sandboxpowered.silica.client.opengl.texture.OpenGLTextureAtlas
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.client.texture.TextureStitcher
import org.sandboxpowered.silica.client.util.stackPush
import org.sandboxpowered.silica.api.util.Identifier

class OpenGLRenderer(private val silica: SilicaClient) : Renderer {

    private val window: Window
        get() = silica.window

    override val name: String = "OpenGL"
    override val version: Version = Version.forIntegers(0, 1, 0)

    private lateinit var obj: OpenGLVBO

    override lateinit var textureAtlas: TextureAtlas

    private val map = HashMap<Identifier, BlockModelFormat>()

    private lateinit var textureStitcher: TextureStitcher

    override fun createTextureStitcher(): TextureStitcher {
        GL.createCapabilities()
        val maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE)
        GL.createCapabilities()
        textureStitcher = TextureStitcher(maxSize, maxSize, false)
        return textureStitcher
    }

    override fun init() {
        textureStitcher.stitch()
        textureAtlas = OpenGLTextureAtlas(textureStitcher)

        stackPush { stack ->
            val vbo = OpenGLVBO.builder(GL_QUADS, stack.malloc(RenderingFormats.POSITION_TEXTURE.getArraySize(300)))

            obj = vbo.build()
        }

        GL11.glClearColor(0.15f, 0.2f, 0.25f, 0f)
    }

    override fun frame() {
        if (window.resized) {
            glViewport(0, 0, window.width, window.height)
            window.resized = false
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        GL11.glEnable(GL_DEPTH_TEST)
        GL11.glEnable(GL_TEXTURE_2D)
        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)

        GlobalUniform.update(silica)

        textureAtlas.bind()

        RenderingFormats.POSITION_TEXTURE.begin(silica.assetManager)
        RenderingFormats.POSITION_TEXTURE.shader!!["diffuseMap"] = 0
        RenderingFormats.POSITION_TEXTURE.shader!!["normalMap"] = 1
        RenderingFormats.POSITION_TEXTURE.render(obj)
        RenderingFormats.POSITION_TEXTURE.end()

        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun cleanup() {
        textureAtlas.destroy()

        obj.destroy()

        RenderingFormats.POSITION_TEXTURE.shader!!.destroy()
    }

    class OpenGLRenderingFactory : RenderingFactory {
        override val priority: Int = 600
        override val name: String = "opengl"

        override fun createRenderer(silica: SilicaClient): Renderer = OpenGLRenderer(silica)
    }
}
