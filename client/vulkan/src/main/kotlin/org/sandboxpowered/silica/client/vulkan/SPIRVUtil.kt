package org.sandboxpowered.silica.client.vulkan

import org.lwjgl.system.MemoryUtil.NULL
import org.lwjgl.system.NativeResource
import org.lwjgl.util.shaderc.Shaderc.*
import java.io.IOException
import java.lang.ClassLoader.getSystemClassLoader
import java.net.URI
import java.net.URISyntaxException
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths


class SPIRVUtil {
    companion object {
        fun compileShaderFile(shaderFile: String, shaderKind: ShaderKind): SPIRV {
            return compileShaderAbsoluteFile(
                getSystemClassLoader().getResource(shaderFile).toExternalForm(),
                shaderKind
            )
        }

        fun compileShaderAbsoluteFile(shaderFile: String, shaderKind: ShaderKind): SPIRV {
            try {
                val source = String(Files.readAllBytes(Paths.get(URI(shaderFile))))
                return compileShader(shaderFile, source, shaderKind)
            } catch (e: IOException) {
                throw java.lang.RuntimeException(e)
            } catch (e: URISyntaxException) {
                throw java.lang.RuntimeException(e)
            }
        }

        fun compileShader(filename: String, source: String?, shaderKind: ShaderKind): SPIRV {
            val compiler = shaderc_compiler_initialize()
            if (compiler == NULL) {
                throw RuntimeException("Failed to create shader compiler")
            }
            val result: Long = shaderc_compile_into_spv(compiler, source, shaderKind.kind, filename, "main", NULL)
            if (result == NULL) {
                throw RuntimeException("Failed to compile shader $filename into SPIR-V")
            }
            if (shaderc_result_get_compilation_status(result) != shaderc_compilation_status_success) {
                throw RuntimeException(
                    """Failed to compile shader ${filename}into SPIR-V:
${shaderc_result_get_error_message(result)}"""
                )
            }
            shaderc_compiler_release(compiler)
            return SPIRV(result, shaderc_result_get_bytes(result))
        }
    }

    enum class ShaderKind(val kind: Int) {
        VERTEX_SHADER(shaderc_glsl_vertex_shader),
        GEOMETRY_SHADER(shaderc_glsl_geometry_shader),
        FRAGMENT_SHADER(shaderc_glsl_fragment_shader);
    }

    class SPIRV(private val handle: Long, bytecode: ByteBuffer?) : NativeResource {
        var bytecode: ByteBuffer?

        override fun free() {
            shaderc_result_release(handle)
            bytecode = null // Help the GC
        }

        init {
            this.bytecode = bytecode
        }
    }
}