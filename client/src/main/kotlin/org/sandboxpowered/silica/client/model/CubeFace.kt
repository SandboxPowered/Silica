package org.sandboxpowered.silica.client.model

import org.sandboxpowered.silica.api.util.Direction

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

    operator fun get(corner: Int): Corner = cornerArray[corner]

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

        fun getCorner(direction: Direction, corner: Int): Corner = DIRECTION_LOOKUP[direction.id][corner]
    }

    data class Corner(val x: Int, val y: Int, val z: Int)
}