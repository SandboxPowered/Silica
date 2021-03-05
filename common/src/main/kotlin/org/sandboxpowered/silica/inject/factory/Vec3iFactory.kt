package org.sandboxpowered.silica.inject.factory

import org.sandboxpowered.api.util.math.Position
import org.sandboxpowered.api.util.math.Vec3i

class Vec3iFactory : Vec3i.Factory {

    override fun create(x: Int, y: Int, z: Int): Vec3i {
        return Position.create(x, y, z)
    }
}