package org.sandboxpowered.quartz.platform

import imgui.ImGui

class DebugRenderer {
    fun newWindow(title: String, windowBlock: DebugWindow.() -> Unit) {
        ImGui.begin(title)
        val window = DebugWindow()
        windowBlock(window)
        ImGui.end()
    }

    class DebugWindow {
        fun text(text: String) = ImGui.text(text)
        fun space() = ImGui.spacing()
        fun separator() = ImGui.separator()
        fun button(label: String, block: () -> Unit) = ImGui.button(label).also { if (it) block() }
    }
}