package org.sandboxpowered.silica.client.gl

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.opengl.GL.createCapabilities
import org.lwjgl.system.MemoryUtil.NULL
import org.sandboxpowered.quartz.platform.Window
import kotlin.properties.Delegates

class GLWindow(windowTitle: String, override var width: Int, override var height: Int) : Window {
    private var initalized: Boolean = false

    override var title: String = windowTitle
        set(value) {
            field = value
            if (initalized)
                glfwSetWindowTitle(handle, value)
        }

    override var handle: Long by Delegates.notNull()

    override var fps = 0
        private set

    override var visible: Boolean = false
        set(value) {
            if (field != value) {
                if (value) glfwShowWindow(handle) else glfwHideWindow(handle)
                field = value
            }
        }

    override val shouldClose: Boolean
        get() = glfwWindowShouldClose(handle)

    fun create() {
        GLFWErrorCallback.createPrint(System.err).set()
        val errors = ArrayList<String>()
        glfwSetErrorCallback { err, desc ->
            errors += "GLFW error during init: [0x$err] $desc"
        }
        check(glfwInit()) { "Failed to initialize GLFW, errors: ${errors.joinToString(",")}" }
        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        glfwWindowHint(GLFW_CLIENT_API, GLFW_OPENGL_API)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2)
        handle = glfwCreateWindow(width, height, title, NULL, NULL)
        check(handle != NULL) { "Failed to create window" }
        glfwSetWindowSizeCallback(handle) { win, w, h ->
            if (win == handle) {
                width = w
                height = h
            }
        }
        glfwMakeContextCurrent(handle)
        glfwSwapInterval(1)
        createCapabilities()
        initalized = true
    }

    override fun close() = glfwSetWindowShouldClose(handle, true)

    override fun destroy() {
        glfwDestroyWindow(handle)
        glfwTerminate()
    }
}