package org.sandboxpowered.silica.client.opengl

import org.joml.Matrix4fStack
import org.joml.Vector3f
import org.joml.Vector3fc
import org.sandboxpowered.silica.client.Silica

object GlobalUniform {
    val MODEL = Matrix4fStack(50)
    val VIEW = Matrix4fStack(50)
    val PROJECTION = Matrix4fStack(50)
    val HEIGHT_SCALE = 0f
    val POSITION = Vector3f(0f, 0f, 0f)
    var time = 0

    fun update(client: Silica) {
        time++

        MODEL.clear()
        VIEW.clear()
        PROJECTION.clear()
        PROJECTION.setPerspective(
            Math.toRadians(75.0).toFloat(),
            client.window.width.toFloat() / client.window.height.toFloat(),
            1f,
            100f
        )
        VIEW.rotateXYZ(
            Math.toRadians(45.0).toFloat(),
            Math.toRadians(45.0).toFloat(),
            Math.toRadians(45.0).toFloat()
        ).translate(-5f,-5f,-100f)
    }
}

private operator fun Vector3f.unaryMinus(): Vector3fc = Vector3f(-x, -y, -z)
