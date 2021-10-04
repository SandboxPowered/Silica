package org.sandboxpowered.silica.client.model

import org.sandboxpowered.silica.util.content.Direction

enum class CubeFace(vararg corners: Corner) {
    DOWN(
        Corner(Direction.WEST.id, Direction.DOWN.id, Direction.SOUTH.id),
        Corner(Direction.WEST.id, Direction.DOWN.id, Direction.NORTH.id),
        Corner(Direction.EAST.id, Direction.DOWN.id, Direction.NORTH.id),
        Corner(Direction.EAST.id, Direction.DOWN.id, Direction.SOUTH.id)
    ),
    UP(
        Corner(Direction.WEST.id, Direction.UP.id, Direction.NORTH.id),
        Corner(Direction.WEST.id, Direction.UP.id, Direction.SOUTH.id),
        Corner(Direction.EAST.id, Direction.UP.id, Direction.SOUTH.id),
        Corner(Direction.EAST.id, Direction.UP.id, Direction.NORTH.id)
    ),
    NORTH(
        Corner(Direction.EAST.id, Direction.UP.id, Direction.NORTH.id),
        Corner(Direction.EAST.id, Direction.DOWN.id, Direction.NORTH.id),
        Corner(Direction.WEST.id, Direction.DOWN.id, Direction.NORTH.id),
        Corner(Direction.WEST.id, Direction.UP.id, Direction.NORTH.id)
    ),
    SOUTH(
        Corner(Direction.WEST.id, Direction.UP.id, Direction.SOUTH.id),
        Corner(Direction.WEST.id, Direction.DOWN.id, Direction.SOUTH.id),
        Corner(Direction.EAST.id, Direction.DOWN.id, Direction.SOUTH.id),
        Corner(Direction.EAST.id, Direction.UP.id, Direction.SOUTH.id)
    ),
    WEST(
        Corner(Direction.WEST.id, Direction.UP.id, Direction.NORTH.id),
        Corner(Direction.WEST.id, Direction.DOWN.id, Direction.NORTH.id),
        Corner(Direction.WEST.id, Direction.DOWN.id, Direction.SOUTH.id),
        Corner(Direction.WEST.id, Direction.UP.id, Direction.SOUTH.id)
    ),
    EAST(
        Corner(Direction.EAST.id, Direction.UP.id, Direction.SOUTH.id),
        Corner(Direction.EAST.id, Direction.DOWN.id, Direction.SOUTH.id),
        Corner(Direction.EAST.id, Direction.DOWN.id, Direction.NORTH.id),
        Corner(Direction.EAST.id, Direction.UP.id, Direction.NORTH.id)
    );

    private val cornerArray = corners

    open fun getCorner(corner: Int): Corner {
        return cornerArray[corner]
    }

    companion object {
        private val DIRECTION_LOOKUP = Array(6) {
            when (it) {
                Direction.DOWN.id -> DOWN
                Direction.UP.id -> UP
                Direction.NORTH.id -> NORTH
                Direction.SOUTH.id -> SOUTH
                Direction.EAST.id -> EAST
                Direction.WEST.id -> WEST
                else -> error("Unknown direction $it")
            }
        }

        fun byDirection(direction: Direction): CubeFace = DIRECTION_LOOKUP[direction.id]
    }

    data class Corner(val x: Int, val y: Int, val z: Int)
}