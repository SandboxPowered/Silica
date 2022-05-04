package org.sandboxpowered.silica.api.ecs.component

import com.artemis.PooledComponent
import org.joml.Vector3f

class VelocityComponent : PooledComponent() {
    val direction: Vector3f = Vector3f()
    var velocity: Float = 0f

    override fun reset() {
        direction.set(0.0, 0.0, 0.0)
        velocity = 0f
    }
}