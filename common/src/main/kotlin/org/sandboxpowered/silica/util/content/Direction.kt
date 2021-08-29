package org.sandboxpowered.silica.util.content

import com.google.common.collect.Iterators
import org.jetbrains.annotations.Range
import org.joml.Vector3i
import org.sandboxpowered.silica.state.property.StringSerializable
import java.util.function.Predicate
import kotlin.math.abs

enum class Direction(
    val id: Int,
    val invertedId: Int,
    val horizontalId: Int,
    private val dirName: String,
    val axisDirection: AxisDirection,
    val axis: Axis,
    val offset: Vector3i,
) : StringSerializable {
    DOWN(0, 1, -1, "down", AxisDirection.NEGATIVE, Axis.Y, Vector3i(0, -1, 0)),
    UP(1, 0, -1, "up", AxisDirection.POSITIVE, Axis.Y, Vector3i(0, 1, 0)),
    NORTH(2, 3, 2, "north", AxisDirection.NEGATIVE, Axis.Z, Vector3i(0, 0, -1)),
    SOUTH(3, 2, 0, "south", AxisDirection.POSITIVE, Axis.Z, Vector3i(0, 0, 1)),
    WEST(4, 5, 1, "west", AxisDirection.NEGATIVE, Axis.X, Vector3i(-1, 0, 0)),
    EAST(5, 4, 3, "east", AxisDirection.POSITIVE, Axis.X, Vector3i(1, 0, 0));

    val offsetX: Int
        get() = this.offset.x
    val offsetY: Int
        get() = this.offset.y
    val offsetZ: Int
        get() = this.offset.z

    override fun getName(): String = this.dirName

    override fun toString(): String = this.dirName

    companion object {
        val ALL = values()
        val NAME_MAP = ALL.associateBy { it.name }
        val ID_TO_DIRECTION = ALL.sortedWith(Comparator.comparingInt { it.id }).toTypedArray()
        val HORIZONTAL = ALL.filter { it.axis.isHorizontal }
            .sortedWith(Comparator.comparingInt { it.horizontalId })
            .toTypedArray()

        fun byId(idx: @Range(from = 0, to = 5) Int): Direction {
            require(idx in 0..5) { "Direction id can only be within 0-5" }
            return ID_TO_DIRECTION[idx]
        }

        fun byHorizontalId(idx: @Range(from = 0, to = 3) Int): Direction {
            require(idx in 0..5) { "Horizontal Direction id can only be within 0-3" }
            return HORIZONTAL[abs(idx % 3)]
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

    enum class Axis(private val axisName: String) : Predicate<Direction>, StringSerializable {
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

        override fun getName(): String = axisName

        override fun toString(): String = axisName

        override fun test(t: Direction): Boolean = t.axis == this
    }
}