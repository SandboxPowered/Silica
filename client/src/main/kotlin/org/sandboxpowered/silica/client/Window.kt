package org.sandboxpowered.silica.client

import org.lwjgl.glfw.GLFW
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryUtil

class Window(private var windowName: String, private var width: Int, private var height: Int) {
    private val internalPointer: Long
    var currentFps = 0
        private set
    private var fpsCounter = 0
    private var nextDebugInfoUpdateTime = System.currentTimeMillis()

    fun cleanup() {
        GLFW.glfwDestroyWindow(internalPointer)
        GLFW.glfwTerminate()
    }

    fun update() {
        GLFW.glfwSwapBuffers(internalPointer)
        GLFW.glfwPollEvents()
        limitDisplayFPS(144)
        ++fpsCounter
        while (System.currentTimeMillis() >= nextDebugInfoUpdateTime + 1000L) {
            currentFps = fpsCounter
            nextDebugInfoUpdateTime += 1000L
            fpsCounter = 0
        }
    }

    fun shouldClose(): Boolean {
        return GLFW.glfwWindowShouldClose(internalPointer)
    }

    fun close() {
        GLFW.glfwSetWindowShouldClose(internalPointer, true)
    }

    fun setTitle(name: String) {
        if (windowName != name) {
            windowName = name
            GLFW.glfwSetWindowTitle(internalPointer, windowName)
        }
    }

    companion object {
        private var lastDrawTime = Double.MIN_VALUE
        fun limitDisplayFPS(fps: Int) {
            val d = lastDrawTime + 1.0 / fps.toDouble()
            var e: Double
            e = GLFW.glfwGetTime()
            while (e < d) {
                GLFW.glfwWaitEventsTimeout(d - e)
                e = GLFW.glfwGetTime()
            }
            lastDrawTime = e
        }
    }

    init {
        GLFW.glfwDefaultWindowHints()
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE)
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE)
        internalPointer = GLFW.glfwCreateWindow(width, height, windowName, MemoryUtil.NULL, MemoryUtil.NULL)
        if (internalPointer == MemoryUtil.NULL) throw UnknownError("Failed to create the GLFW window")
        GLFW.glfwSetKeyCallback(internalPointer) { window, key, scancode, action, mods ->
            if (key == GLFW.GLFW_KEY_ESCAPE && action == GLFW.GLFW_RELEASE)
                GLFW.glfwSetWindowShouldClose(window, true)
        }
        GLFW.glfwSetWindowSizeCallback(internalPointer) { window, newWidth, newHeight ->
            if(window == internalPointer) {
                width = newWidth
                height = newHeight
            }
        }
        MemoryStack.stackPush().use {
            val pWidth = it.mallocInt(1)
            val pHeight = it.mallocInt(1)
            GLFW.glfwGetWindowSize(internalPointer, pWidth, pHeight)
            val vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor())!!
            width = pWidth[0]
            height = pHeight[0]
            GLFW.glfwSetWindowPos(
                internalPointer,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2
            )
        }
        GLFW.glfwMakeContextCurrent(internalPointer)
        GLFW.glfwSwapInterval(1)
        GLFW.glfwShowWindow(internalPointer)
    }
}