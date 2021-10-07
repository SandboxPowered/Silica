package org.sandboxpowered.silica.client.model

import org.joml.*

interface VertexBufferObject {
    fun bind()
    fun destroy()
    interface Builder {
        fun put(vararg data: Byte): Builder
        fun put(vararg data: Short): Builder
        fun put(vararg data: Int): Builder
        fun put(vararg data: Float): Builder
        fun put(vararg data: Double): Builder
        fun put(data: Vector2ic): Builder
        fun put(data: Vector3ic): Builder
        fun put(data: Vector2fc): Builder
        fun put(data: Vector3fc): Builder
        fun put(data: Vector2dc): Builder
        fun put(data: Vector3dc): Builder
        fun vertex(x: Float, y: Float, z: Float, u: Float, v: Float, nx: Float, ny: Float, nz: Float): Builder
        fun build(): VertexBufferObject
    }
}