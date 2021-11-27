package org.sandboxpowered.silica.world.util

import org.sandboxpowered.silica.api.world.WorldSelection

/**
 * Simple square AABB
 */
class Bounds : WorldSelection {
    override var x = 0
        private set
    override var y = 0
        private set
    override var z = 0
        private set
    override var width = 0
        private set
    override var height = 0
        private set
    override var length = 0
        private set

    fun set(
        x: Int, y: Int, z: Int,
        size: Int
    ): Bounds {
        this.x = x
        this.y = y
        this.z = z
        this.width = size
        this.height = size
        this.length = size
        return this
    }

    fun set(
        x: Int, y: Int, z: Int,
        width: Int, height: Int, length: Int
    ): Bounds {
        this.x = x
        this.y = y
        this.z = z
        this.width = width
        this.height = height
        this.length = length
        return this
    }

    override fun toString(): String {
        return "Bounds(x=$x, y=$y, z=$z, width=$width, height=$height, length=$length)"
    }
}