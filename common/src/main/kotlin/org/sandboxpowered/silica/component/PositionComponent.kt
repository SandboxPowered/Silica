package org.sandboxpowered.silica.component

import com.artemis.PooledComponent
import org.joml.Vector3f

class PositionComponent : PooledComponent() {
    val pos: Vector3f = Vector3f()

    override fun reset() {
        pos.set(0f, 0f, 0f)
    }
}