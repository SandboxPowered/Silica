package org.sandboxpowered.silica.client.model

import org.joml.Vector3fc

interface VertexBufferObject {
    fun bind()
    fun destroy()
    interface Builder {
        fun put(vararg data: Byte): Builder
        fun put(vararg data: Short): Builder
        fun put(vararg data: Int): Builder
        fun put(vararg data: Float): Builder
        fun put(vararg data: Double): Builder
        fun put(data: Vector3fc): Builder
        fun build(): VertexBufferObject
    }
}