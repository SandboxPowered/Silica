package org.sandboxpowered.silica.api.world

import org.joml.Vector3fc
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.api.math.plus
import org.sandboxpowered.silica.api.math.times

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
     * Retur!s the smallest [WorldSectionReader] containing the selected region
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

    /**
     * Casts a ray starting at [from] along a normalized [ray] and returns the first hit, if any
     */
    fun rayCast(from: Vector3fc, ray: Vector3fc, max: Float): Float

    /**
     * Casts a ray starting at [from] along a normalized [ray] and returns the first hit, if any
     */
    fun rayCastPoint(from: Vector3fc, ray: Vector3fc, max: Float): Vector3fc? {
        val hit = rayCast(from, ray, max)
        return if (hit < 0) null else ray * hit + from
    }
}