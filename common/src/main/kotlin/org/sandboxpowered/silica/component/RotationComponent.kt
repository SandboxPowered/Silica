package org.sandboxpowered.silica.component

import com.artemis.PooledComponent
import org.joml.Vector3d

class RotationComponent : PooledComponent() {
    var yaw: Float = 0f
    var pitch: Float = 0f

    override fun reset() {
        yaw = 0f
        pitch = 0f
    }

    operator fun component1(): Float = yaw
    operator fun component2(): Float = pitch
}