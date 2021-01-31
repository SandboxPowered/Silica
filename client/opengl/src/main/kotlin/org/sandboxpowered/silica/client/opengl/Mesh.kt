package org.sandboxpowered.silica.client.opengl

import org.lwjgl.opengl.GL30.*
import org.lwjgl.system.MemoryStack.stackPush

class Mesh(private val positions: FloatArray, private val colours: FloatArray, private val indices: IntArray) {
    var vaoId = -1
    var posVboId = -1
    var colourVboId = -1
    var idxVboId = -1
    var vertexCount = -1

    init {
        stackPush().use {
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

            colourVboId = glGenBuffers()
            val colourBuffer = it.mallocFloat(colours.size)
            colourBuffer.put(colours).flip()
            glBindBuffer(GL_ARRAY_BUFFER, colourVboId)
            glBufferData(GL_ARRAY_BUFFER, colourBuffer, GL_STATIC_DRAW)
            glEnableVertexAttribArray(1)
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0)

            idxVboId = glGenBuffers()
            val indicesBuffer = it.mallocInt(indices.size)
            indicesBuffer.put(indices).flip()
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, idxVboId)
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW)

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
    }

    fun cleanup() {
        glDisableVertexAttribArray(0);

        // Delete the VBOs
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glDeleteBuffers(posVboId);
        glDeleteBuffers(colourVboId);
        glDeleteBuffers(idxVboId);

        // Delete the VAO
        glBindVertexArray(0);
        glDeleteVertexArrays(vaoId);
    }
}