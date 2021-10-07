package org.sandboxpowered.silica.client.texture

import org.joml.Vector2fc
import org.lwjgl.stb.STBImage.*
import org.sandboxpowered.silica.client.util.stackPush
import org.sandboxpowered.silica.client.util.streamToBuffer
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ResourceType
import org.sandboxpowered.silica.util.Identifier
import java.io.InputStream
import java.nio.ByteBuffer

interface TextureAtlas : Texture {
    companion object {
        val BLOCK_ATLAS = Identifier("minecraft", "block_atlas_location")
        val MISSING_TEXTURE = Identifier("silica", "missing_texture")
    }

    fun getSprite(id: Identifier): Sprite?

    interface Sprite {
        val id: Identifier
        val minUV: Vector2fc
        val maxUV: Vector2fc

        operator fun component1(): Identifier
        operator fun component2(): Vector2fc
        operator fun component3(): Vector2fc
    }

    data class Reference(val atlas: Identifier, val texture: Identifier)

    data class SpriteReference(
        val id: Identifier,
        val width: Int,
        val height: Int,
        val albedo: SpriteData,
        val normal: SpriteData?,
        val glow: SpriteData?,
        val specular: SpriteData?
    ) {
        companion object {
            operator fun invoke(id: Identifier, manager: ResourceManager): SpriteReference {
                val albedo =
                    SpriteData.streamToImageBuffer(manager.open(ResourceType.ASSETS, id.affix("textures/", ".png")))
                val normal = manager.tryOpen(ResourceType.ASSETS, id.affix("textures/", "_n.png"))
                    ?.let(SpriteData.Companion::streamToImageBuffer)
                val glow = manager.tryOpen(ResourceType.ASSETS, id.affix("textures/", "_g.png"))
                    ?.let(SpriteData.Companion::streamToImageBuffer)
                val specular = manager.tryOpen(ResourceType.ASSETS, id.affix("textures/", "_s.png"))
                    ?.let(SpriteData.Companion::streamToImageBuffer)

                return SpriteReference(id, albedo.width, albedo.height, albedo, normal, glow, specular)
            }

            fun requireAllSameSize(spriteData: SpriteData, vararg other: SpriteData) {
                other.forEach {
                    require(spriteData.width == it.width && spriteData.height == it.height) { "Sprite data incorrect size" }
                }
            }
        }
    }

    data class SpriteData(
        val width: Int,
        val height: Int,
        val components: Int,
        val hdr: Boolean,
        val image: ByteBuffer
    ) {
        val isSquare: Boolean = width == height

        companion object {
            fun streamToImageBuffer(stream: InputStream): SpriteData {
                return stackPush {
                    val w = it.mallocInt(1)
                    val h = it.mallocInt(1)
                    val comp = it.mallocInt(1)

                    val imageBuffer = streamToBuffer(it, stream, 2048)

                    if (!stbi_info_from_memory(imageBuffer, w, h, comp))
                        error("Failed to read image information: ${stbi_failure_reason()}")

                    val image = stbi_load_from_memory(imageBuffer, w, h, comp, 4)
                        ?: error("Failed to load image ${stbi_failure_reason()}")

                    SpriteData(w.get(), h.get(), comp.get(), stbi_is_hdr_from_memory(imageBuffer), image)
                }
            }
        }

    }
}