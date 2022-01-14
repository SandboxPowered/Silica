package org.sandboxpowered.silica.client.opengl.shader

import org.joml.Matrix4fc
import org.joml.Vector2fc
import org.joml.Vector3fc
import org.lwjgl.opengl.GL20.*
import org.sandboxpowered.utilities.Identifier
import org.sandboxpowered.silica.client.shader.Shader
import org.sandboxpowered.silica.client.util.stackPush
import org.sandboxpowered.silica.resources.ResourceManager
import org.sandboxpowered.silica.resources.ResourceType.ASSETS
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.util.stream.Collectors


class OpenGLShader(vertexFile: String, fragmentFile: String) : Shader {
    companion object {
        operator fun invoke(manager: ResourceManager, id: Identifier): OpenGLShader {
            val vertexFile = streamToString(manager.open(ASSETS, id.affix("shaders/", ".vert")))
                ?: error("Failed to find vertex file for shader $id")
            val fragmentFile = streamToString(manager.open(ASSETS, id.affix("shaders/", ".frag")))
                ?: error("Failed to find fragment file for shader $id")
            return OpenGLShader(vertexFile, fragmentFile)
        }

        private fun streamToString(stream: InputStream?): String? {
            if (stream == null) return null
            InputStreamReader(stream).use { isr ->
                BufferedReader(isr).use { reader ->
                    return reader.lines().collect(Collectors.joining(System.lineSeparator()))
                }
            }
        }
    }

    val programID = glCreateProgram()
    private val vertexID = glCreateShader(GL_VERTEX_SHADER)
    private val fragmentID = glCreateShader(GL_FRAGMENT_SHADER)

    init {
        glShaderSource(vertexID, vertexFile)
        glCompileShader(vertexID)

        if (glGetShaderi(vertexID, GL_COMPILE_STATUS) == GL_FALSE) {
            error("Vertex Shader: ${glGetShaderInfoLog(vertexID)}")
        }

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

    private fun location(name: String): Int = glGetUniformLocation(programID, name)

    override fun set(uniform: String, value: Float) = glUniform1f(location(uniform), value)

    override fun set(uniform: String, value: Int) = glUniform1i(location(uniform), value)

    override fun set(uniform: String, value: Boolean) = set(uniform, if (value) 1 else 0)

    override fun set(uniform: String, value: Vector2fc) =
        glUniform2f(location(uniform), value.x(), value.y())

    override fun set(uniform: String, value: Vector3fc) =
        glUniform3f(location(uniform), value.x(), value.y(), value.z())

    override fun set(uniform: String, value: Matrix4fc) = stackPush {
        val matrix = it.mallocFloat(16)
        value[matrix]
        glUniformMatrix4fv(location(uniform), false, matrix)
    }

    override fun bind() = glUseProgram(programID)

    override fun unbind() = glUseProgram(0)

    override fun destroy() {
        glDetachShader(programID, vertexID)
        glDetachShader(programID, fragmentID)
        glDeleteShader(vertexID)
        glDeleteShader(fragmentID)
        glDeleteProgram(programID)
    }
}