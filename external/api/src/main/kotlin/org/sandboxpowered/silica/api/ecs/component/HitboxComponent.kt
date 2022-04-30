package org.sandboxpowered.silica.api.ecs.component

import com.artemis.PooledComponent
import org.joml.Vector3f

class HitboxComponent : PooledComponent() {
    val hitbox: Vector3f = Vector3f()

    override fun reset() {
        hitbox.set(0.0, 0.0, 0.0)
    }
}