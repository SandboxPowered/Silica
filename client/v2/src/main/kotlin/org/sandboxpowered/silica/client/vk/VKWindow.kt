package org.sandboxpowered.silica.client.vk

import org.lwjgl.glfw.GLFW
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVulkan
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryUtil
import org.sandboxpowered.quartz.platform.Window
import kotlin.properties.Delegates

class VKWindow(windowTitle: String, override var width: Int, override var height: Int) : Window {
    private var initalized: Boolean = false

    override var title: String = windowTitle
        set(value) {
            field = value
            if (initalized)
                GLFW.glfwSetWindowTitle(handle, value)
        }

    override var handle: Long by Delegates.notNull()

    override var fps = 0
        private set

    override var visible: Boolean = false
        set(value) {
            if (field != value) {
                if (value) GLFW.glfwShowWindow(handle) else GLFW.glfwHideWindow(handle)
                field = value
            }
        }

    override val shouldClose: Boolean
        get() = GLFW.glfwWindowShouldClose(handle)

    fun create() {
        GLFWErrorCallback.createPrint(System.err).set()
        val errors = ArrayList<String>()
        GLFW.glfwSetErrorCallback { err, desc ->
            errors += "GLFW error during init: [0x$err] $desc"
        }
        check(GLFWVulkan.glfwVulkanSupported()) { "Vulkan is not supported" }
        check(GLFW.glfwInit()) { "Failed to initialize GLFW, errors: ${errors.joinToString(",")}" }
        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_CLIENT_API, GLFW.GLFW_NO_API)
        handle = GLFW.glfwCreateWindow(width, height, title, MemoryUtil.NULL, MemoryUtil.NULL)
        check(handle != MemoryUtil.NULL) { "Failed to create window" }
        GLFW.glfwSetWindowSizeCallback(handle) { win, w, h ->
            if (win == handle) {
                width = w
                height = h
            }
        }
        GLFW.glfwMakeContextCurrent(handle)
        GLFW.glfwSwapInterval(1)
        initalized = true
    }

    override fun close() = GLFW.glfwSetWindowShouldClose(handle, true)

    override fun destroy() {
        GLFW.glfwDestroyWindow(handle)
        GLFW.glfwTerminate()
    }
}