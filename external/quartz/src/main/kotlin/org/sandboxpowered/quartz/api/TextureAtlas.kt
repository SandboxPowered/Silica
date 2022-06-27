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
        val layers: Map<TextureLayer, SpriteLayerData>
    )

    data class SpriteLayerData(
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