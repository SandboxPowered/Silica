package org.sandboxpowered.silica.client.opengl

import com.github.zafarkhaja.semver.Version
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL30.*
import org.sandboxpowered.silica.client.*
import org.sandboxpowered.silica.client.model.JSONModel
import org.sandboxpowered.silica.client.model.jsonModelGson
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.client.texture.TextureStitcher
import org.sandboxpowered.silica.client.util.stackPush
import org.sandboxpowered.silica.resources.ResourceType
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.content.Direction
import java.io.InputStreamReader

class OpenGLRenderer(private val silica: Silica) : Renderer {

    private val window: Window
        get() = silica.window

    override val name: String = "OpenGL"
    override val version: Version = Version.forIntegers(0, 1, 0)

    private val fov = Math.toRadians(60.0).toFloat()

    private val zNear = 0.01f

    private val zFar = 1000f

    private lateinit var obj: VertexBufferObject
    private val transforms = Transforms()

    private lateinit var atlas: TextureAtlas

    override fun init() {
        GL.createCapabilities()

        val maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE)
        val stitcher = TextureStitcher(maxSize, maxSize, false)

        val func: (Identifier) -> JSONModel = {
            jsonModelGson.fromJson(
                InputStreamReader(
                    silica.assetManager.open(
                        ResourceType.ASSETS,
                        Identifier(it.namespace, "models/${it.path}.json")
                    )
                ),
                JSONModel::class.java
            )
        }

        val modelJson = func(Identifier("block/cut_copper_stairs"))

        modelJson.getReferences(func).forEach {
            stitcher.add(
                TextureAtlas.SpriteData(
                    it.texture, silica.assetManager.open(
                        ResourceType.ASSETS,
                        Identifier(it.texture.namespace, "textures/${it.texture.path}.png")
                    )
                )
            )
        }

        stitcher.stitch()
        atlas = OpenGLTextureAtlas(stitcher)

        val size = 1

        stackPush { stack ->
            val vbo = VertexBufferObject.builder(
                GL_TRIANGLES,
                stack.malloc(DefaultRenderingFormat.POSITION_TEXTURE.getArraySize(
                    6 * (modelJson.getElements().sumOf { it.faces.size } * 6)
                ))
            )

            val x = 0
            val y = 0
            val z = 0
            modelJson.getElements().forEach {
                it.faces.forEach { (dir, face) ->
                    val sprite = atlas.getSprite(modelJson.resolve(face.texture).texture)!!
                    val (_, minUV, maxUV) = sprite

                    val differenceBetweenX = maxUV.x() - minUV.x()
                    val differenceBetweenY = maxUV.y() - minUV.y()

                    val stitcherWidth = stitcher.width / 16f
                    val stitcherHeight = stitcher.height / 16f

                    val u2Sprite = face.textureData.uvs!![2] / 16f
                    val v2Sprite = face.textureData.uvs!![3] / 16f

                    val u2 = (face.textureData.uvs!![0] / 16f / stitcherWidth) + minUV.x()
                    val v2 = (face.textureData.uvs!![1] / 16f / stitcherHeight) + minUV.y()
                    val u1 = minUV.x() + (differenceBetweenX * u2Sprite)
                    val v1 = minUV.y() + (differenceBetweenY * v2Sprite)

                    when (dir) {
                        Direction.UP -> {
                            vbo.put(it.from.x / 16f + x, it.to.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.from.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u1, v2)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                            vbo.put(it.from.x / 16f + x, it.to.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.from.z / 16f + z).put(u2, v1)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                        }
                        Direction.DOWN -> {
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.to.z / 16f + z).put(u1, v2)
                            vbo.put(it.to.x / 16f + x, it.from.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.to.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u2, v1)
                            vbo.put(it.to.x / 16f + x, it.from.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                        }
                        Direction.WEST -> {
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.from.x / 16f + x, it.to.y / 16f + y, it.from.z / 16f + z).put(u1, v2)
                            vbo.put(it.from.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.to.z / 16f + z).put(u2, v1)
                            vbo.put(it.from.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                        }
                        Direction.EAST -> {
                            vbo.put(it.to.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.from.z / 16f + z).put(u1, v2)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                            vbo.put(it.to.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.to.x / 16f + x, it.from.y / 16f + y, it.to.z / 16f + z).put(u2, v1)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                        }
                        Direction.SOUTH -> {
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.to.z / 16f + z).put(u1, v1)
                            vbo.put(it.from.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u1, v2)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.to.z / 16f + z).put(u1, v1)
                            vbo.put(it.to.x / 16f + x, it.from.y / 16f + y, it.to.z / 16f + z).put(u2, v1)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.to.z / 16f + z).put(u2, v2)
                        }
                        Direction.NORTH -> {
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.from.x / 16f + x, it.to.y / 16f + y, it.from.z / 16f + z).put(u1, v2)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.from.z / 16f + z).put(u2, v2)
                            vbo.put(it.from.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u1, v1)
                            vbo.put(it.to.x / 16f + x, it.from.y / 16f + y, it.from.z / 16f + z).put(u2, v1)
                            vbo.put(it.to.x / 16f + x, it.to.y / 16f + y, it.from.z / 16f + z).put(u2, v2)
                        }
                    }
                }
            }

            obj = vbo.build()
        }

        GL11.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
    }

    override fun frame() {
        if (window.resized) {
            glViewport(0, 0, window.width, window.height)
            window.resized = false
        }

        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT or GL11.GL_DEPTH_BUFFER_BIT)
        GL11.glEnable(GL_DEPTH_TEST)
        GL11.glEnable(GL_TEXTURE_2D)

        GlobalUniform.update(silica)

        glActiveTexture(GL_TEXTURE0)
        atlas.bind()

        DefaultRenderingFormat.POSITION_TEXTURE.begin(silica.assetManager)
        DefaultRenderingFormat.POSITION_TEXTURE.shader?.setUniform("diffuseMap", 0);
        DefaultRenderingFormat.POSITION_TEXTURE.render(obj)
        DefaultRenderingFormat.POSITION_TEXTURE.end()

        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun cleanup() {
        atlas.destroy()

        obj.destroy()
    }

    class OpenGLRenderingFactory : RenderingFactory {
        override val priority: Int = 600
        override val name: String = "opengl"

        override fun createRenderer(silica: Silica): Renderer = OpenGLRenderer(silica)
    }
}
