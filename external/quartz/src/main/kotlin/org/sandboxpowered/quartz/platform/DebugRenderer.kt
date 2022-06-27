package org.sandboxpowered.quartz.platform

import imgui.ImGui
import imgui.ImGuiIO

class DebugRenderer {
    fun newWindow(title: String, windowBlock: DebugWindow.() -> Unit) {
        ImGui.begin(title)
        val window = DebugWindow()
        windowBlock(window)
        ImGui.end()
    }

    class DebugWindow {
        val io: ImGuiIO
            get() = ImGui.getIO()
        fun text(text: String) = ImGui.text(text)
        fun space() = ImGui.spacing()
        fun separator() = ImGui.separator()
        fun button(label: String, block: () -> Unit) = ImGui.button(label).also { if (it) block() }
        fun collapsable(label: String, block: () -> Unit) = ImGui.collapsingHeader(label).also { if(it) block() }
        fun checkbox(label: String, value: Boolean) = ImGui.checkbox(label, value)
    }

    fun test() {
        // ImGUI
        ImGui.begin("Debug")

        ImGui.text("FPS: ${ImGui.getIO().framerate.toInt()}")

        if (ImGui.collapsingHeader("Debug Render")) {
            if (ImGui.button("Boat Collision")) {
                Debug.renderBoatCollision = !Debug.renderBoatCollision
            }
            if (ImGui.button("Boat Visual")) {
                Debug.renderBoat = !Debug.renderBoat
            }
            if (ImGui.button("Ocean")) {
                Debug.renderOcean = !Debug.renderOcean
            }
            if (ImGui.button("Boat Floaters")) {
                Debug.renderBoatFloaters = !Debug.renderBoatFloaters
            }
            ImGui.separator()
        }

        ImGui.end()

        // Quartz Engine
        newWindow("Debug") {
            text("FPS: ${io.framerate.toInt()}")

            collapsable("Debug Render") {
                button("Boat Collision") {
                    Debug.renderBoatCollision = !Debug.renderBoatCollision
                }
                button("Boat Visual") {
                    Debug.renderBoat = !Debug.renderBoat
                }
                button("Ocean") {
                    Debug.renderOcean = !Debug.renderOcean
                }
                button("Boat Floaters") {
                    Debug.renderBoatFloaters = !Debug.renderBoatFloaters
                }
                separator()
            }
        }
    }
}

object Debug {
    var renderOcean = false
    var renderBoat = false
    var renderBoatFloaters = false
    var renderBoatCollision = false

    var overrideChunkPackets = false
}