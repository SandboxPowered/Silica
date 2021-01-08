package org.sandboxpowered.silica.util.math

import org.sandboxpowered.api.util.Direction
import org.sandboxpowered.api.util.math.Position

class Position(
    private val x: Int,
    private val y: Int,
    private val z: Int
) : Position {
    override fun getX() = x
    override fun getY() = y
    override fun getZ() = z

    override fun add(x: Int, y: Int, z: Int): Position {
        TODO("Not yet implemented")
    }

    override fun sub(x: Int, y: Int, z: Int): Position {
        TODO("Not yet implemented")
    }

    override fun toMutable(): Position.Mutable {
        TODO("Not yet implemented")
    }

    override fun toImmutable(): Position {
        TODO("Not yet implemented")
    }

    override fun offset(direction: Direction?, amount: Int): Position {
        TODO("Not yet implemented")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is org.sandboxpowered.silica.util.math.Position) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }
}