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
        return Position(this.x + x, this.y + y, this.z + z)
    }

    override fun sub(x: Int, y: Int, z: Int): Position {
        return add(-x, -y, -z)
    }

    override fun toMutable(): Position.Mutable {
        return Position.Mutable.create(x, y, z)
    }

    override fun toImmutable(): Position {
        return this
    }

    override fun offset(direction: Direction, amount: Int): Position {
        return add(amount * direction.offsetX, amount * direction.offsetY, amount * direction.offsetZ)
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

    class Mutable(
        private var x: Int,
        private var y: Int,
        private var z: Int
    ) : Position.Mutable {
        override fun getX() = x
        override fun getY() = y
        override fun getZ() = z

        override fun add(x: Int, y: Int, z: Int): Position.Mutable {
            this.x += x
            this.y += y
            this.z += z
            return this
        }

        override fun sub(x: Int, y: Int, z: Int): Position.Mutable {
            this.x -= x
            this.y -= y
            this.z -= z
            return this
        }

        override fun toMutable(): Position.Mutable {
            return this
        }

        override fun toImmutable(): Position {
            return Position.create(x, y, z)
        }

        override fun offset(direction: Direction, amount: Int): Position {
            return add(
                amount * direction.offsetX,
                amount * direction.offsetY,
                amount * direction.offsetZ,
            )
        }

        override fun set(x: Int, y: Int, z: Int): Position.Mutable {
            this.x = x
            this.y = y
            this.z = z
            return this
        }
    }
}