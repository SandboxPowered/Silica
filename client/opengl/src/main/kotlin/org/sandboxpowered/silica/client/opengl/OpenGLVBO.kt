package org.sandboxpowered.silica.client.opengl

import org.joml.Vector3fc
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL30.glBindVertexArray
import org.lwjgl.opengl.GL30.glGenVertexArrays
import org.sandboxpowered.silica.client.model.VertexBufferObject
import java.nio.*

class OpenGLVBO(val type: Int, val size: Int, data: ByteBuffer) : VertexBufferObject {
    private val iD: Int = glGenBuffers()
    private val vaoID: Int = glGenVertexArrays()

    override fun bind() {
        glBindVertexArray(vaoID)
        glBindBuffer(GL_ARRAY_BUFFER, iD)
    }

    override fun destroy() = glDeleteBuffers(iD)

    class OpenGLVBOBuilder internal constructor(private val type: Int, buffer: ByteBuffer) :
        VertexBufferObject.Builder {
        private val byteBuffer: ByteBuffer
        private val rawIntBuffer: IntBuffer
        private val rawShortBuffer: ShortBuffer
        private val rawFloatBuffer: FloatBuffer
        private val rawDoubleBuffer: DoubleBuffer
        private var size = 0

        override fun build(): OpenGLVBO = OpenGLVBO(type, size, byteBuffer)

        override fun put(vararg data: Byte): OpenGLVBOBuilder {
            size += data.size * java.lang.Byte.BYTES
            byteBuffer.put(data)
            return this
        }

        override fun put(vararg data: Short): OpenGLVBOBuilder {
            size += data.size * java.lang.Short.BYTES
            rawShortBuffer.put(data)
            return this
        }

        override fun put(vararg data: Int): OpenGLVBOBuilder {
            size += data.size * Integer.BYTES
            rawIntBuffer.put(data)
            return this
        }

        override fun put(data: Vector3fc): OpenGLVBOBuilder = put(data.x(), data.y(), data.z())

        override fun put(vararg data: Float): OpenGLVBOBuilder {
            size += data.size * java.lang.Float.BYTES
            rawFloatBuffer.put(data)
            return this
        }

        override fun put(vararg data: Double): OpenGLVBOBuilder {
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
        fun builder(type: Int, buffer: ByteBuffer) = OpenGLVBOBuilder(type, buffer)
    }

    init {
        bind()
        glBufferData(GL_ARRAY_BUFFER, data, GL_STATIC_DRAW)
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindVertexArray(0)
    }
}