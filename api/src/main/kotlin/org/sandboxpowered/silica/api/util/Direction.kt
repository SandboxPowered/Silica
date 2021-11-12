package org.sandboxpowered.silica.api.util

import com.google.common.collect.Iterators
import org.jetbrains.annotations.Range
import org.joml.Vector3i
import org.joml.Vector3ic
import java.util.function.Predicate
import kotlin.math.abs
import kotlin.math.floor

enum class Direction(
    val id: Int,
    private val inverse: Int,
    val horizontalId: Int,
    override val asString: String,
    val axisDirection: AxisDirection,
    val axis: Axis,
    val offset: Vector3ic,
) : StringSerializable {
    DOWN(0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, Vector3i(0, -1, 0)),
    UP(1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, Vector3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, Vector3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, Vector3i(0, 0, 1)),
    WEST(4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, Vector3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, Vector3i(1, 0, 0));

    val opposite: Direction by lazy { byId(inverse) }
    val offsetX: Int = this.offset.x()
    val offsetY: Int = this.offset.y()
    val offsetZ: Int = this.offset.z()

    override fun toString(): String = this.asString

    companion object {
        val ALL = values()
        val NAME_MAP = ALL.associateBy { it.asString }
        val ID_TO_DIRECTION = ALL.sortedWith(Comparator.comparingInt { it.id }).toTypedArray()
        val HORIZONTAL = ALL.filter { it.axis.isHorizontal }
            .sortedWith(Comparator.comparingInt { it.horizontalId })
            .toTypedArray()

        fun byId(idx: @Range(from = 0, to = 5) Int): Direction {
            require(idx in 0..5) { "Direction id can only be within 0-5 got $idx" }
            return ID_TO_DIRECTION[idx]
        }

        fun byHorizontalId(idx: @Range(from = 0, to = 3) Int): Direction {
            require(idx in 0..3) { "Horizontal Direction id can only be within 0-3 got $idx" }
            return HORIZONTAL[idx]
        }

        fun byName(name: String): Direction? = NAME_MAP[name.lowercase()]
        fun fromYRotation(yaw: Float): Direction {
            println("${floor(yaw / 90.0 +0.5)} | ${floor(yaw / 90.0+0.5).toInt() and 3}")
            val out = byHorizontalId(floor(yaw / 90.0+0.5).toInt() and 3)
            println("Got $yaw out $out")
            return out
        }
    }


    enum class Type(private vararg val facingArray: Direction) :
        Predicate<Direction>, Iterable<Direction> {
        HORIZONTAL(NORTH, EAST, SOUTH, WEST),
        VERTICAL(UP, DOWN);

        override fun test(direction: Direction): Boolean {
            return direction.axis.type == this
        }

        override fun iterator(): Iterator<Direction> {
            return Iterators.forArray(*facingArray)
        }
    }

    enum class AxisDirection(val offset: Int, val description: String) {
        POSITIVE(1, "Towards positive"),
        NEGATIVE(-1, "Towards negative");
    }

    enum class Axis(override val asString: String) : Predicate<Direction>, StringSerializable {
        X("x"),
        Y("y"),
        Z("z");

        val isVertical: Boolean
            get() = this == Y
        val isHorizontal: Boolean
            get() = this != Y
        val type: Type
            get() = when (Y) {
                this -> Type.VERTICAL
                else -> Type.HORIZONTAL
            }

        override fun toString(): String = asString

        override fun test(t: Direction): Boolean = t.axis == this
    }
}