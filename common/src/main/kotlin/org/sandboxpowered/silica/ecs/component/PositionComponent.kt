package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent
import org.joml.Vector3d

class PositionComponent : PooledComponent() {
    val pos: Vector3d = Vector3d()

    override fun reset() {
        pos.set(0.0, 0.0, 0.0)
    }
}