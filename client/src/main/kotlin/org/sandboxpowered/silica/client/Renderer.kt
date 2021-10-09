package org.sandboxpowered.silica.client

import com.github.zafarkhaja.semver.Version
import org.sandboxpowered.silica.client.texture.TextureAtlas
import org.sandboxpowered.silica.client.texture.TextureStitcher

interface Renderer {
    val name: String
    val version: Version

    fun initWindowHints() = Unit
    fun init() = Unit
    fun frame() = Unit
    fun cleanup() = Unit
    fun createTextureStitcher(): TextureStitcher

    val textureAtlas: TextureAtlas
}