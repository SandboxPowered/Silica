package org.sandboxpowered.silica.client

interface Renderer {
    fun initWindowHints() = Unit
    fun init() = Unit
    fun frame() = Unit
    fun getName(): String
}