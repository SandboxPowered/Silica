package org.sandboxpowered.silica.client.opengl.texture

import org.joml.Vector2f
import org.joml.Vector2fc
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL30
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.client.texture.TextureAtlas.Sprite
import org.sandboxpowered.silica.client.texture.TextureStitcher
import org.sandboxpowered.silica.util.Identifier


class OpenGLTextureAtlas(private val stitcher: TextureStitcher) : TextureAtlas {
    private val sprites: MutableMap<Identifier, Sprite> = HashMap()
    private val albedoMapId = glGenTextures()
    private val normalMapId = glGenTextures()

    init {
        glBindTexture(GL_TEXTURE_2D, albedoMapId)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glPixelStorei(GL_PACK_ALIGNMENT, 1)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, stitcher.width, stitcher.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)
        stitcher.loopBranches { data, x, y ->
            sprites[data.id] = OpenGLSprite(
                data.id,
                Vector2f(x.toFloat() / stitcher.width, y.toFloat() / stitcher.height),
                Vector2f((x + data.width).toFloat() / stitcher.width, (y + data.height).toFloat() / stitcher.height)
            )
            glTexSubImage2D(
                GL_TEXTURE_2D,
                0,
                x,
                y,
                data.width,
                data.height,
                GL_RGBA,
                GL_UNSIGNED_BYTE,
                data.albedo.image
            )
        }
        glBindTexture(GL_TEXTURE_2D, normalMapId)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1)
        glPixelStorei(GL_PACK_ALIGNMENT, 1)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, stitcher.width, stitcher.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0)
        stitcher.loopBranches { data, x, y ->
            if (data.normal != null) {
                glTexSubImage2D(
                    GL_TEXTURE_2D,
                    0,
                    x,
                    y,
                    data.width,
                    data.height,
                    GL_RGBA,
                    GL_UNSIGNED_BYTE,
                    data.normal!!.image
                )
            }
        }
    }

    override fun getSprite(id: Identifier): Sprite? = sprites[id]

    override fun bind() {
        GL30.glActiveTexture(GL30.GL_TEXTURE0)
        glBindTexture(GL_TEXTURE_2D, albedoMapId)
        GL30.glActiveTexture(GL30.GL_TEXTURE1)
        glBindTexture(GL_TEXTURE_2D, normalMapId)
    }

    override fun destroy() {
        glDeleteTextures(albedoMapId)
        glDeleteTextures(normalMapId)
    }
}

data class OpenGLSprite(
    override val id: Identifier,
    override val minUV: Vector2fc,
    override val maxUV: Vector2fc
) : Sprite