package org.sandboxpowered.quartz.platform

typealias InputListener = (key: Int, scancode: Int, action: Int, mods: Int) -> Unit

interface Input {
    fun create(window: Window)
    fun update()
    fun destroy()

    fun addListener(listener: InputListener)

    fun isKeyPressed(key: Int): Boolean
    fun isKeyHeld(key: Int): Boolean
    fun isKeyReleased(key: Int): Boolean

    fun isMouseButtonPressed(button: Int): Boolean
    fun isMouseButtonHeld(button: Int): Boolean
    fun isMouseButtonReleased(button: Int): Boolean
}