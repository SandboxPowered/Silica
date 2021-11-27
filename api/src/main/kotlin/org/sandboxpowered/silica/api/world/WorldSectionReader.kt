package org.sandboxpowered.silica.api.world

import org.sandboxpowered.silica.api.world.state.block.BlockState

/**
 * Represents a read-only section of the world.
 * Coordinates are world coordinates.
 */
interface WorldSectionReader {
    val selection: WorldSelection

    /**
     * Returns the state at given position
     */
    operator fun get(x: Int, y: Int, z: Int): BlockState

    /**
     * Returns the smallest [WorldSectionReader] containing the selected region
     */
    operator fun get(
        x: Int, y: Int, z: Int,
        width: Int, height: Int, depth: Int
    ): WorldSectionReader

    /**
     * Amount of non-air blocks in this section
     */
    fun nonAirInSection(x: Int, y: Int, z: Int): Int

    fun contains(x: Int, y: Int, z: Int): Boolean = selection.contains(x, y, z)
}