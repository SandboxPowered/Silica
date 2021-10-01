package org.sandboxpowered.silica.client.opengl

import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL15
import org.lwjgl.opengl.GL20
import org.lwjgl.opengl.GL30
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.util.Identifier

class RenderingFormat(identity: Identifier, vararg attributes: Attribute) {
    private val identity: Identifier
    val attributes: Array<Attribute>

    var shader: IodineShader? = null
        private set

    fun begin(manager: ResourceManager) {
        if (shader == null) {
            shader = IodineShader(manager, identity)
        }
        shader?.bind()
    }

    fun render(vbo: VertexBufferObject) {
        shader?.setUniform("model", GlobalUniform.MODEL)
        shader?.setUniform("view", GlobalUniform.VIEW)
        shader?.setUniform("projection", GlobalUniform.PROJECTION)
        shader?.setUniform("heightScale", GlobalUniform.HEIGHT_SCALE)
        shader?.setUniform("viewPos", GlobalUniform.POSITION)
        vbo.bindVAO()
        vbo.bindVBO()
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

    fun getArraySize(vertices: Int): Int {
        return dataSize * vertices
    }

    fun setupFormat() {
        val stride = dataSize
        var offset = 0
        val programID: Int = shader!!.programID
        for (attribute in attributes) {
            val attrib = attribute.attributeLocation
            GL20.glEnableVertexAttribArray(attrib)
            attribute.specifyVertexAttributes(attrib, stride, offset)
            offset += attribute.dataSize
        }
    }

    enum class DataType(val id: Int, val key: String, val size: Int) {
        BYTE(GL11.GL_BYTE, "Byte", java.lang.Byte.BYTES),
        UBYTE(GL11.GL_UNSIGNED_BYTE, "Unsigned Byte", java.lang.Byte.BYTES),
        SHORT(GL11.GL_SHORT, "Short", java.lang.Short.BYTES),
        USHORT(GL11.GL_UNSIGNED_SHORT, "Unsigned Short", java.lang.Short.BYTES),
        INT(GL11.GL_INT, "Int", Integer.BYTES),
        UINT(GL11.GL_UNSIGNED_INT, "Unsigned Int", Integer.BYTES),
        FLOAT(GL11.GL_FLOAT, "Float", java.lang.Float.BYTES),
        DOUBLE(GL11.GL_DOUBLE, "Double", java.lang.Double.BYTES);

    }

    class Attribute(
        val attributeName: String,
        val attributeLocation: Int,
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