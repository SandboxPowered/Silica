package org.sandboxpowered.silica.client.model

import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.util.content.Direction

data class BakedQuad(
    val vertexData: FloatArray,
    val tintIndex: Int,
    val face: Direction,
    val sprite: TextureAtlas.Sprite,
    val shade: Boolean
) {
    val hasColour: Boolean = tintIndex != -1

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as BakedQuad

        if (!vertexData.contentEquals(other.vertexData)) return false
        if (tintIndex != other.tintIndex) return false
        if (face != other.face) return false
        if (sprite != other.sprite) return false
        if (shade != other.shade) return false

        return true
    }

    override fun hashCode(): Int {
        var result = vertexData.contentHashCode()
        result = 31 * result + tintIndex
        result = 31 * result + face.hashCode()
        result = 31 * result + sprite.hashCode()
        result = 31 * result + shade.hashCode()
        return result
    }
}
