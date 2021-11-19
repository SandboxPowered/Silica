package org.sandboxpowered.silica.api.world

import org.sandboxpowered.silica.api.world.state.block.BlockState

/**
 * Represents a section of the world
 * Coordinates are world coordinates.
 */
interface WorldSection : WorldSectionReader {
    /**
     * Sets the target position to [state].
     * Throws an [IllegalArgumentException] if the given position is out of bounds.
     */
    operator fun set(
        x: Int, y: Int, z: Int,
        state: BlockState
    )

    /**
     * Returns the smallest [WorldSection] containing the selected region
     */
    override operator fun get(
        x: Int, y: Int, z: Int,
        width: Int, height: Int, depth: Int
    ): WorldSection
}