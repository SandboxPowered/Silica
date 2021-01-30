package org.sandboxpowered.silica.client

import org.lwjgl.glfw.GLFW.*
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class Window(private var windowName: String, var width: Int, var height: Int, val renderer: Renderer) {
    val internalPointer: Long
    var currentFps = 0
        private set
    private var fpsCounter = 0
    private var nextDebugInfoUpdateTime = System.currentTimeMillis()
    var resized = false

    fun cleanup() {
        glfwDestroyWindow(internalPointer)
        glfwTerminate()
    }

    fun update() {
        glfwSwapBuffers(internalPointer)
        glfwPollEvents()
        limitDisplayFPS(144)
        ++fpsCounter
        while (System.currentTimeMillis() >= nextDebugInfoUpdateTime + 1000L) {
            currentFps = fpsCounter
            nextDebugInfoUpdateTime += 1000L
            fpsCounter = 0
        }
    }

    fun shouldClose(): Boolean {
        return glfwWindowShouldClose(internalPointer)
    }

    fun close() {
        glfwSetWindowShouldClose(internalPointer, true)
    }

    fun setTitle(name: String) {
        if (windowName != name) {
            windowName = name
            glfwSetWindowTitle(internalPointer, windowName)
        }
    }

    companion object {
        private var lastDrawTime = Double.MIN_VALUE
        fun limitDisplayFPS(fps: Int) {
            val d = lastDrawTime + 1.0 / fps.toDouble()
            var e: Double
            e = glfwGetTime()
            while (e < d) {
                glfwWaitEventsTimeout(d - e)
                e = glfwGetTime()
            }
            lastDrawTime = e
        }
    }

    init {
        glfwDefaultWindowHints()

        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE)
        renderer.initWindowHints()
        internalPointer = glfwCreateWindow(width, height, windowName, MemoryUtil.NULL, MemoryUtil.NULL)
        if (internalPointer == MemoryUtil.NULL) throw UnknownError("Failed to create the GLFW window")
        glfwSetKeyCallback(internalPointer) { window, key, scancode, action, mods ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
                glfwSetWindowShouldClose(window, true)
        }
        glfwSetWindowSizeCallback(internalPointer) { window, newWidth, newHeight ->
            if(window == internalPointer) {
                width = newWidth
                height = newHeight
                resized=true
            }
        }
        MemoryStack.stackPush()
        glfwMakeContextCurrent(internalPointer)
        glfwSwapInterval(1)
        glfwShowWindow(internalPointer)
    }
}