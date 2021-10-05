package org.sandboxpowered.silica.client.opengl

import com.github.zafarkhaja.semver.Version
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import org.sandboxpowered.silica.client.Renderer
import org.sandboxpowered.silica.client.RenderingFactory
import org.sandboxpowered.silica.client.SilicaClient
import org.sandboxpowered.silica.client.Window
import org.sandboxpowered.silica.client.model.BakedQuadCreator
import org.sandboxpowered.silica.client.model.JSONModel
import org.sandboxpowered.silica.client.model.jsonModelGson
import org.sandboxpowered.silica.client.opengl.texture.OpenGLTextureAtlas
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.client.texture.TextureStitcher
import org.sandboxpowered.silica.client.util.stackPush
import org.sandboxpowered.silica.resources.ResourceType.ASSETS
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.extensions.minus
import java.io.InputStreamReader

class OpenGLRenderer(private val silica: SilicaClient) : Renderer {

    private val window: Window
        get() = silica.window

    override val name: String = "OpenGL"
    override val version: Version = Version.forIntegers(0, 1, 0)

    private lateinit var obj: OpenGLVBO

    private lateinit var atlas: TextureAtlas

    private val map = HashMap<Identifier, JSONModel>()

    override fun init() {
        GL.createCapabilities()

        val maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE)
        val stitcher = TextureStitcher(maxSize, maxSize, false)

        val func: (Identifier) -> JSONModel = {
            map.computeIfAbsent(it) {
                jsonModelGson.fromJson(
                    InputStreamReader(
                        silica.assetManager.open(
                            ASSETS,
                            Identifier(it.namespace, "models/${it.path}.json")
                        )
                    ),
                    JSONModel::class.java
                )
            }
        }

        val modelJson = func(Identifier("block/lectern"))

        modelJson.getReferences(func).forEach {
            stitcher.add(
                TextureAtlas.SpriteData(
                    it.texture,
                    silica.assetManager.open(
                        ASSETS,
                        Identifier(it.texture.namespace, "textures/${it.texture.path}.png")
                    )
                )
            )
        }

        stitcher.stitch()
        atlas = OpenGLTextureAtlas(stitcher)

        stackPush { stack ->
            val vbo = OpenGLVBO.builder(
                GL_QUADS,
                stack.malloc(DefaultRenderingFormat.POSITION_TEXTURE.getArraySize(300))
            )

            modelJson.getElements().forEach {
                it.faces.forEach { (dir, face) ->
                    val sprite = atlas.getSprite(modelJson.resolve(face.texture).texture)!!
                    val (_, minUV, maxUV) = sprite

                    val uvDifference = maxUV - minUV

                    val quad = BakedQuadCreator.CREATOR.bake(it.from, it.to, face, sprite, dir, it.rotation, it.shade)

                    val vertexCount = quad.vertexData.size / 8

                    for (vert in 0 until vertexCount) {
                        val idx = vert * 8

                        vbo.vertex(
                            quad.vertexData[idx],
                            quad.vertexData[idx + 1],
                            quad.vertexData[idx + 2],
                            minUV.x() + (quad.vertexData[idx + 3] / 16f) * uvDifference.x(),
                            minUV.y() + (quad.vertexData[idx + 4] / 16f) * uvDifference.y()
                        )
                        // TODO: control this through shader and do animated textures through shader
                    }
                }
            }

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

        glActiveTexture(GL_TEXTURE0)
        atlas.bind()

        DefaultRenderingFormat.POSITION_TEXTURE.begin(silica.assetManager)
        DefaultRenderingFormat.POSITION_TEXTURE.shader!!["diffuseMap"] = 0
        DefaultRenderingFormat.POSITION_TEXTURE.render(obj)
        DefaultRenderingFormat.POSITION_TEXTURE.end()

        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun cleanup() {
        atlas.destroy()

        obj.destroy()

        DefaultRenderingFormat.POSITION_TEXTURE.shader!!.destroy()
    }

    class OpenGLRenderingFactory : RenderingFactory {
        override val priority: Int = 600
        override val name: String = "opengl"

        override fun createRenderer(silica: SilicaClient): Renderer = OpenGLRenderer(silica)
    }
}
