package org.sandboxpowered.silica.inject.factory

import org.sandboxpowered.api.util.math.Position
import org.sandboxpowered.silica.util.math.Position as SilicaPosition

class PositionFactory : Position.Factory {
    override fun immutable(x: Int, y: Int, z: Int): Position = SilicaPosition(x, y, z)

    override fun mutable(x: Int, y: Int, z: Int): Position.Mutable = SilicaPosition.Mutable(x, y, z)
}