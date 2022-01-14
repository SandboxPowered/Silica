package org.sandboxpowered.silica.api.util.math

import org.sandboxpowered.silica.api.util.Direction
import kotlin.math.log2

open class Position(
    open val x: Int,
    open val y: Int,
    open val z: Int
) : Comparable<Position> {
    open fun add(x: Int, y: Int, z: Int): Position = Position(this.x + x, this.y + y, this.z + z)

    open fun sub(x: Int, y: Int, z: Int): Position = add(-x, -y, -z)

    open fun toMutable(): Mutable = Mutable(x, y, z)

    open fun toImmutable(): Position = this

    fun toChunkPosition(): ChunkPosition = ChunkPosition(x shr 4, y shr 4, z shr 4)

    open fun shift(direction: Direction, amount: Int): Position =
        add(amount * direction.offsetX, amount * direction.offsetY, amount * direction.offsetZ)

    open fun add(position: Position): Position = add(position.x, position.y, position.z)

    open fun sub(position: Position): Position = sub(position.x, position.y, position.z)

    open fun shift(direction: Direction): Position = shift(direction, 1)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Position) return false

        if (x != other.x) return false
        if (y != other.y) return false
        if (z != other.z) return false

        return true
    }

    override fun compareTo(other: Position): Int {
        val xCompare = x.compareTo(other.x)
        if (xCompare != 0) return xCompare
        val yCompare = y.compareTo(other.y)
        if (yCompare != 0) return yCompare
        return z.compareTo(other.z)
    }

    override fun hashCode(): Int {
        var result = x
        result = 31 * result + y
        result = 31 * result + z
        return result
    }

    override fun toString(): String = "[$x,$y,$z]"

    val packed: Long
        get() = packIntoLong(x, y, z)

    companion object {
        private val SIZE_BITS_X = 1 + log2(smallestEncompassingPowerOfTwo(30000000).toDouble()).toInt()
        private val SIZE_BITS_Z = SIZE_BITS_X
        private val SIZE_BITS_Y = 64 - SIZE_BITS_X - SIZE_BITS_Z
        private val BITS_X = (1L shl SIZE_BITS_X) - 1L
        private val BITS_Y = (1L shl SIZE_BITS_Y) - 1L
        private val BITS_Z = (1L shl SIZE_BITS_Z) - 1L
        private val BIT_SHIFT_Z = SIZE_BITS_Y
        private val BIT_SHIFT_X = SIZE_BITS_Y + SIZE_BITS_Z

        private fun smallestEncompassingPowerOfTwo(value: Int): Int {
            var i = value - 1
            i = i or (i shr 1)
            i = i or (i shr 2)
            i = i or (i shr 4)
            i = i or (i shr 8)
            i = i or (i shr 16)
            return i + 1
        }

        private fun unpackLongX(packedPos: Long): Int =
            (packedPos shl 64 - BIT_SHIFT_X - SIZE_BITS_X shr 64 - SIZE_BITS_X).toInt()

        private fun unpackLongY(packedPos: Long): Int = (packedPos shl 64 - SIZE_BITS_Y shr 64 - SIZE_BITS_Y).toInt()

        private fun unpackLongZ(packedPos: Long): Int =
            (packedPos shl 64 - BIT_SHIFT_Z - SIZE_BITS_Z shr 64 - SIZE_BITS_Z).toInt()

        private fun packIntoLong(x: Int, y: Int, z: Int): Long {
            var l = 0L
            l = l or (x.toLong() and BITS_X shl BIT_SHIFT_X)
            l = l or (y.toLong() and BITS_Y shl 0)
            l = l or (z.toLong() and BITS_Z shl BIT_SHIFT_Z)
            return l
        }

        fun unpack(long: Long): Position = Position(unpackLongX(long), unpackLongY(long), unpackLongZ(long))
    }

    operator fun rangeTo(other: Position) = PositionRange(this, other)

    class Mutable(
        override var x: Int,
        override var y: Int,
        override var z: Int,
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

        override fun toMutable(): Mutable = this

        override fun toImmutable(): Position = Position(x, y, z)

        override fun shift(direction: Direction, amount: Int): Position = add(
            amount * direction.offsetX,
            amount * direction.offsetY,
            amount * direction.offsetZ,
        )

        override fun add(position: Position): Position = add(position.x, position.y, position.z)

        override fun sub(position: Position): Position = sub(position.x, position.y, position.z)

        override fun shift(direction: Direction): Position = shift(direction, 1)

        fun set(x: Int, y: Int, z: Int): Mutable {
            this.x = x
            this.y = y
            this.z = z
            return this
        }
    }

    class PositionRange(override val start: Position, private val end: Position) : ClosedRange<Position> {
        override val endInclusive: Position
            get() = end

        override fun contains(value: Position): Boolean =
            start.x <= value.x && start.y <= value.y && start.z <= value.z && end.x >= value.x && end.y >= value.y && end.z >= value.z

        override fun isEmpty(): Boolean = false

        override fun toString(): String = "$start-$end"
    }
}