package org.sandboxpowered.silica.api.ecs.component

import com.artemis.PooledComponent
import org.joml.Vector3f

class HitboxComponent : PooledComponent() {
    val hitbox: Vector3f = Vector3f(.6f, 1.8f, .6f) // stuff assumes hitbox is either 0 or strictly positive

    override fun reset() {
        hitbox.set(.6f, 1.8f, .6f)
    }
}