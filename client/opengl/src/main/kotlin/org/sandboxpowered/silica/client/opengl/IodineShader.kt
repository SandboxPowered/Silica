package org.sandboxpowered.silica.client.opengl

import org.joml.Matrix4f
import org.joml.Vector2f
import org.joml.Vector3f
import org.lwjgl.opengl.GL20.*
import org.sandboxpowered.silica.client.util.stackPush
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ResourceType.ASSETS
import org.sandboxpowered.silica.util.Identifier
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors


class IodineShader(val vertexFile: String, val fragmentFile: String) {
    companion object {
        operator fun invoke(manager: ResourceManager, id: Identifier): IodineShader {
            val vertexFile = streamToString(manager.open(ASSETS, Identifier(id.namespace, "shaders/${id.path}.vert")))
                ?: error("Failed to find vertex file for shader $id")
            val fragmentFile = streamToString(manager.open(ASSETS, Identifier(id.namespace, "shaders/${id.path}.frag")))
                ?: error("Failed to find fragment file for shader $id")
            return IodineShader(vertexFile, fragmentFile)
        }

        fun streamToString(stream: InputStream?): String? {
            if (stream == null) return null
            InputStreamReader(stream).use { isr ->
                BufferedReader(isr).use { reader ->
                    return reader.lines().collect(Collectors.joining(System.lineSeparator()))
                }
            }
        }
    }

    private var vertexID = 0
    private var fragmentID = 0
    var programID = 0

    init {
        create()
    }

    private fun create() {
        programID = glCreateProgram()
        vertexID = glCreateShader(GL_VERTEX_SHADER)

        glShaderSource(vertexID, vertexFile)
        glCompileShader(vertexID)

        if (glGetShaderi(vertexID, GL_COMPILE_STATUS) == GL_FALSE) {
            error("Vertex Shader: ${glGetShaderInfoLog(vertexID)}")
        }

        fragmentID = glCreateShader(GL_FRAGMENT_SHADER)

        glShaderSource(fragmentID, fragmentFile)
        glCompileShader(fragmentID)

        if (glGetShaderi(fragmentID, GL_COMPILE_STATUS) == GL_FALSE) {
            error("Fragment Shader: ${glGetShaderInfoLog(fragmentID)}")
        }

        glAttachShader(programID, vertexID)
        glAttachShader(programID, fragmentID)

        glLinkProgram(programID)
        if (glGetProgrami(programID, GL_LINK_STATUS) == GL_FALSE) {
            error("Program Linking: ${glGetProgramInfoLog(programID)}")
        }

        glValidateProgram(programID)
        if (glGetProgrami(programID, GL_VALIDATE_STATUS) == GL_FALSE) {
            error("Program Validation: ${glGetProgramInfoLog(programID)}")
        }

        glDeleteShader(vertexID)
        glDeleteShader(fragmentID)
    }

    fun getUniformLocation(name: String): Int {
        return glGetUniformLocation(programID, name)
    }

    fun setUniform(name: String, value: Float) {
        glUniform1f(getUniformLocation(name), value)
    }

    fun setUniform(name: String, value: Int) {
        glUniform1i(getUniformLocation(name), value)
    }

    fun setUniform(name: String, value: Boolean) {
        setUniform(name, if (value) 1 else 0)
    }

    fun setUniform(name: String, value: Vector2f) {
        glUniform2f(getUniformLocation(name), value.x(), value.y())
    }

    fun setUniform(name: String, value: Vector3f) {
        glUniform3f(getUniformLocation(name), value.x(), value.y(), value.z())
    }

    fun setUniform(name: String, value: Matrix4f) {
        stackPush {
            val matrix = it.mallocFloat(16)
            value[matrix]
            glUniformMatrix4fv(getUniformLocation(name), false, matrix)
        }
    }

    fun bind() {
        glUseProgram(programID)
    }

    fun unbind() {
        glUseProgram(0)
    }

    fun destroy() {
        glDetachShader(programID, vertexID)
        glDetachShader(programID, fragmentID)
        glDeleteShader(vertexID)
        glDeleteShader(fragmentID)
        glDeleteProgram(programID)
    }
}