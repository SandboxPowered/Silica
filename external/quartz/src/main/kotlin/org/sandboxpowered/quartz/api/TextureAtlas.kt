package org.sandboxpowered.quartz.api

import org.joml.Vector2fc
import org.sandboxpowered.utilities.Identifier
import java.nio.ByteBuffer

interface TextureAtlas : Texture {
    operator fun get(id: Identifier): Sprite?

    data class Sprite(val id: Identifier, val minUV: Vector2fc, val maxUV: Vector2fc)
    data class Reference(val id: Identifier, val sprite: Sprite)
    data class SpriteReference(
        val id: Identifier,
        val width: Int,
        val height: Int,
        val sprites: Array<SpriteData>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SpriteReference

            if (id != other.id) return false
            if (width != other.width) return false
            if (height != other.height) return false
            if (!sprites.contentEquals(other.sprites)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = id.hashCode()
            result = 31 * result + width
            result = 31 * result + height
            result = 31 * result + sprites.contentHashCode()
            return result
        }
    }

    data class SpriteData(
        val id: Identifier,
        val width: Int,
        val height: Int,
        val components: Int,
        val hdr: Boolean,
        val bytes: ByteBuffer
    ) {
        val isSquare = width == height
    }
}