package org.sandboxpowered.silica.component

import com.artemis.PooledComponent
import org.joml.Vector3d
import org.joml.Vector3f

class PositionComponent : PooledComponent() {
    val pos: Vector3d = Vector3d()

    override fun reset() {
        pos.set(0.0, 0.0, 0.0)
    }
}