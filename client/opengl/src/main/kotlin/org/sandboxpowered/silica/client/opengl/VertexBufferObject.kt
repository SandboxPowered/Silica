package org.sandboxpowered.silica.client.opengl

import org.joml.Vector3f
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL30
import org.lwjgl.opengl.GL30.glBindVertexArray
import java.nio.*


class VertexBufferObject(val type: Int, val size: Int, data: ByteBuffer?) {
    val iD: Int
    private val vaoID: Int
    fun bindVAO() {
        glBindVertexArray(vaoID)
    }

    fun destroy() {
        glDeleteBuffers(iD)
    }

    fun bindVBO() {
        glBindBuffer(GL_ARRAY_BUFFER, iD)
    }

    class Builder internal constructor(private val type: Int, buffer: ByteBuffer) {
        private val byteBuffer: ByteBuffer
        private val rawIntBuffer: IntBuffer
        private val rawShortBuffer: ShortBuffer
        private val rawFloatBuffer: FloatBuffer
        private val rawDoubleBuffer: DoubleBuffer
        private var size = 0
        fun build(): VertexBufferObject {
            return VertexBufferObject(type, size, byteBuffer)
        }

        fun put(vararg data: Byte): Builder {
            size += data.size * java.lang.Byte.BYTES
            byteBuffer.put(data)
            return this
        }

        fun put(vararg data: Short): Builder {
            size += data.size * java.lang.Short.BYTES
            rawShortBuffer.put(data)
            return this
        }

        fun put(vararg data: Int): Builder {
            size += data.size * Integer.BYTES
            rawIntBuffer.put(data)
            return this
        }

        fun put(data: Vector3f): Builder {
            return put(data.x, data.y, data.z)
        }

        fun put(vararg data: Float): Builder {
            size += data.size * java.lang.Float.BYTES
            rawFloatBuffer.put(data)
            return this
        }

        fun put(vararg data: Double): Builder {
            size += data.size * java.lang.Double.BYTES
            rawDoubleBuffer.put(data)
            return this
        }

        init {
            byteBuffer = buffer
            rawIntBuffer = buffer.asIntBuffer()
            rawShortBuffer = buffer.asShortBuffer()
            rawFloatBuffer = buffer.asFloatBuffer()
            rawDoubleBuffer = buffer.asDoubleBuffer()
        }
    }

    companion object {
        fun builder(type: Int, buffer: ByteBuffer): Builder = Builder(type, buffer)
    }

    init {
        vaoID = GL30.glGenVertexArrays()
        bindVAO()
        iD = glGenBuffers()
        bindVBO()
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }
}