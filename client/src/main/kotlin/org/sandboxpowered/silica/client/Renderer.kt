package org.sandboxpowered.silica.client

import com.github.zafarkhaja.semver.Version

interface Renderer {
    val name: String
    val version: Version

    fun initWindowHints() = Unit
    fun init() = Unit
    fun frame() = Unit
    fun cleanup() = Unit
}