package org.sandboxpowered.silica.api.ecs.component

import com.artemis.PooledComponent
import org.joml.Vector3d

class VelocityComponent : PooledComponent() {
    val velocity: Vector3d = Vector3d()

    override fun reset() {
        velocity.set(0.0, 0.0, 0.0)
    }
}