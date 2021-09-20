package org.sandboxpowered.silica.client.opengl

import org.lwjgl.opengl.GL30.*
import org.sandboxpowered.silica.client.util.stackPush

class Mesh(private val positions: FloatArray, private val texCoords: FloatArray, private val indices: IntArray) {
    var vaoId = -1
    var posVboId = -1
    var texCoordsVboId = -1
    var idxVboId = -1
    var vertexCount = -1

    init {
        stackPush {
            val vertBuffer = it.mallocFloat(positions.size)
            vertexCount = indices.size
            vertBuffer.put(positions).flip()

            vaoId = glGenVertexArrays()
            glBindVertexArray(vaoId)

            posVboId = glGenBuffers()
            val posBuffer = it.mallocFloat(positions.size)
            posBuffer.put(positions).flip()

            glBindBuffer(GL_ARRAY_BUFFER, posVboId)
            glBufferData(GL_ARRAY_BUFFER, posBuffer, GL_STATIC_DRAW)
            glEnableVertexAttribArray(0)
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0)

            texCoordsVboId = glGenBuffers()
            val texCoordsBuffer = it.mallocFloat(texCoords.size)
            texCoordsBuffer.put(texCoords).flip()
            glBindBuffer(GL_ARRAY_BUFFER, texCoordsVboId)
            glBufferData(GL_ARRAY_BUFFER, texCoordsBuffer, GL_STATIC_DRAW)
            glEnableVertexAttribArray(1)
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 0, 0)

            idxVboId = glGenBuffers()
            val indicesBuffer = it.mallocInt(indices.size)
            indicesBuffer.put(indices).flip()
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)

            glBindBuffer(GL_ARRAY_BUFFER, 0)
            glBindVertexArray(0)
        }
    }

    fun cleanup() {
        glDisableVertexAttribArray(0)

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glDeleteBuffers(posVboId)
        glDeleteBuffers(texCoordsVboId)
        glDeleteBuffers(idxVboId)

        // Delete the VAO
        glBindVertexArray(0)
        glDeleteVertexArrays(vaoId)
    }
}