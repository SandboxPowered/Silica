package org.sandboxpowered.silica.client.input

import org.lwjgl.glfw.GLFW
import org.sandboxpowered.silica.client.Window

class Keyboard(window: Window) {
    private val keys = BooleanArray(GLFW.GLFW_KEY_LAST)

    init {
        GLFW.glfwSetKeyCallback(window.internalPointer) { win, key, scancode, action, mods ->
            if (win == window.internalPointer)
                keys[key] = action != GLFW.GLFW_RELEASE
        }
    }

    fun isKeyDown(key: Int): Boolean = keys[key]
}