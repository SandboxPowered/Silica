package org.sandboxpowered.silica.util.math

import org.sandboxpowered.silica.util.content.Direction

open class Position(
    open val x: Int,
    open val y: Int,
    open val z: Int
) {
    open fun add(x: Int, y: Int, z: Int): Position {
        return Position(this.x + x, this.y + y, this.z + z)
    }

    open fun sub(x: Int, y: Int, z: Int): Position {
        return add(-x, -y, -z)
    }

    open fun toMutable(): Mutable {
        return Mutable(x, y, z)
    }

    open fun toImmutable(): Position {
        return this
    }

    open fun shift(direction: Direction, amount: Int): Position {
        return add(amount * direction.offsetX, amount * direction.offsetY, amount * direction.offsetZ)
    }

    open fun add(position: Position): Position {
        return add(position.x, position.y, position.z)
    }

    open fun sub(position: Position): Position {
        return sub(position.x, position.y, position.z)
    }

    open fun shift(direction: Direction): Position {
        return shift(direction, 1)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Position) return false

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
        override var x: Int,
        override var y: Int,
        override var z: Int
    ) : Position(x, y, z) {

        override fun add(x: Int, y: Int, z: Int): Mutable {
            this.x += x
            this.y += y
            this.z += z
            return this
        }

        override fun sub(x: Int, y: Int, z: Int): Mutable {
            this.x -= x
            this.y -= y
            this.z -= z
            return this
        }

        override fun toMutable(): Mutable {
            return this
        }

        override fun toImmutable(): Position {
            return Position(x, y, z)
        }

        override fun shift(direction: Direction, amount: Int): Position {
            return add(
                amount * direction.offsetX,
                amount * direction.offsetY,
                amount * direction.offsetZ,
            )
        }

        override fun add(position: Position): Position {
            return add(position.x, position.y, position.z)
        }

        override fun sub(position: Position): Position {
            return sub(position.x, position.y, position.z)
        }

        override fun shift(direction: Direction): Position {
            return shift(direction, 1)
        }

        fun set(x: Int, y: Int, z: Int): Mutable {
            this.x = x
            this.y = y
            this.z = z
            return this
        }
    }
}