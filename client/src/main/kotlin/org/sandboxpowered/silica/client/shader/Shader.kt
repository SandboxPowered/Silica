package org.sandboxpowered.silica.client.shader

import org.joml.Matrix4fc
import org.joml.Vector2fc
import org.joml.Vector3fc

interface Shader {
    fun bind()
    fun unbind()
    fun destroy()
    operator fun set(uniform: String, value: Float)
    operator fun set(uniform: String, value: Int)
    operator fun set(uniform: String, value: Boolean)
    operator fun set(uniform: String, value: Vector2fc)
    operator fun set(uniform: String, value: Vector3fc)
    operator fun set(uniform: String, value: Matrix4fc)
}