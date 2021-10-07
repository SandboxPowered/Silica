package org.sandboxpowered.silica.client.opengl

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.sandboxpowered.silica.client.opengl.shader.OpenGLShader
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.util.Identifier

class RenderingFormat(identity: Identifier, vararg attributes: Attribute) {
    private val identity: Identifier
    val attributes: Array<Attribute>

    var shader: OpenGLShader? = null
        private set

    fun begin(manager: ResourceManager) {
        if (shader == null) {
            shader = OpenGLShader(manager, identity)
        }
        shader?.bind()
    }

    fun render(vbo: OpenGLVBO) {
        val shader = shader!!
        shader["model"] = GlobalUniform.MODEL
        shader["view"] = GlobalUniform.VIEW
        shader["projection"] = GlobalUniform.PROJECTION
        shader["heightScale"] = GlobalUniform.HEIGHT_SCALE
        shader["viewPos"] = GlobalUniform.POSITION
        shader["sunDir"] = GlobalUniform.SUN_DIRECTION
        vbo.bind()
        setupFormat()
        GL11.glDrawArrays(vbo.type, 0, vbo.size / dataSize)
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0)
        GL30.glBindVertexArray(0)
    }

    fun end() {
        shader?.unbind()
    }

    val dataSize: Int by lazy {
        var out = 0
        for (attribute in attributes) {
            out += attribute.dataSize
        }
        out
    }

    fun getArraySize(vertices: Int): Int = dataSize * vertices

    fun setupFormat() {
        val stride = dataSize
        var offset = 0
        val programID: Int = shader!!.programID
        for (attribute in attributes) {
            val attrib = GL20.glGetAttribLocation(programID, attribute.attributeName)
            GL20.glEnableVertexAttribArray(attrib)
            attribute.specifyVertexAttributes(attrib, stride, offset)
            offset += attribute.dataSize
        }
    }

    enum class DataType(val id: Int, val key: String, val size: Int) {
        BYTE(GL11.GL_BYTE, "Byte", Byte.SIZE_BYTES),
        UBYTE(GL11.GL_UNSIGNED_BYTE, "Unsigned Byte", UByte.SIZE_BYTES),
        SHORT(GL11.GL_SHORT, "Short", Short.SIZE_BYTES),
        USHORT(GL11.GL_UNSIGNED_SHORT, "Unsigned Short", UShort.SIZE_BYTES),
        INT(GL11.GL_INT, "Int", Int.SIZE_BYTES),
        UINT(GL11.GL_UNSIGNED_INT, "Unsigned Int", UInt.SIZE_BYTES),
        FLOAT(GL11.GL_FLOAT, "Float", Float.SIZE_BYTES),
        DOUBLE(GL11.GL_DOUBLE, "Double", Double.SIZE_BYTES);

    }

    class Attribute(
        val attributeName: String,
        private val vertexSize: Int,
        private val type: DataType,
        private val normalized: Boolean
    ) {
        fun specifyVertexAttributes(attributeId: Int, stride: Int, offset: Int) {
            GL20.glVertexAttribPointer(attributeId, vertexSize, type.id, normalized, stride, offset.toLong())
        }

        val dataSize: Int = type.size * vertexSize
    }

    init {
        this.identity = identity
        this.attributes = arrayOf(*attributes)
    }
}