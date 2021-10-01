package org.sandboxpowered.silica.client.texture

import org.joml.Vector2fc
import org.lwjgl.stb.STBImage.*
import org.sandboxpowered.silica.client.util.stackPush
import org.sandboxpowered.silica.client.util.streamToBuffer
import org.sandboxpowered.silica.util.Identifier
import java.io.InputStream
import java.nio.ByteBuffer

interface TextureAtlas {
    companion object {
        val BLOCK_ATLAS = Identifier("minecraft", "block_atlas_location")
        val MISSING_TEXTURE = Identifier("minecraft", "block/stone")
    }

    fun getSprite(id: Identifier): Sprite?

    interface Sprite {
        val id: Identifier
        val minUV: Vector2fc
        val maxUV: Vector2fc
    }

    data class SpriteData(
        val id: Identifier,
        val width: Int,
        val height: Int,
        val components: Int,
        val hdr: Boolean,
        val image: ByteBuffer
    ) {
        val isSquare: Boolean = width == height

        companion object {
            operator fun invoke(id: Identifier, inputStream: InputStream): SpriteData {
                return stackPush {
                    val w = it.mallocInt(1)
                    val h = it.mallocInt(1)
                    val comp = it.mallocInt(1)

                    val imageBuffer = streamToBuffer(it, inputStream, 2048)

                    if (!stbi_info_from_memory(imageBuffer, w, h, comp))
                        error("Failed to read image information: ${stbi_failure_reason()}")

                    val image = stbi_load_from_memory(imageBuffer, w, h, comp, 4)
                        ?: error("Failed to load image ${stbi_failure_reason()}")

                    SpriteData(id, w.get(), h.get(), comp.get(), stbi_is_hdr_from_memory(imageBuffer), image)
                }
            }
        }

    }
}