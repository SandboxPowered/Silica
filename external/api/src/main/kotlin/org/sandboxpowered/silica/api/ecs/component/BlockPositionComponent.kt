package org.sandboxpowered.silica.api.ecs.component

import com.artemis.PooledComponent
import org.sandboxpowered.silica.api.util.math.Position

class BlockPositionComponent : PooledComponent() {
    private val _pos = Position.Mutable(0, 0, 0)

    val pos: Position
        get() = _pos.toImmutable()

    override fun reset() {
        _pos.set(0, 0, 0)
    }
}