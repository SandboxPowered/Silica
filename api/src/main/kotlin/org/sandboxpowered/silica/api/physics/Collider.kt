package org.sandboxpowered.silica.api.physics

import org.joml.Vector3fc

interface Collider {
    /**
     * Whether this collider can be used to collide with other colliders.
     */
    var enabled: Boolean

    /**
     * Is this collider a trigger?
     *
     * Triggers do not cause physical collisions, instead they are used to detect when an intersection occurs.
     * Useful for blocks such as pressure plates where you want to know when the player is standing on the block.
     */
    var isTrigger: Boolean

    /**
     * World space bounding volume of the collider
     *
     * @return Empty [AxisAlignedBox] if the collider is disabled
     */
    val bounds: AxisAlignedBox

    fun cast(start: Vector3fc, direction: Vector3fc, maxDistance: Float): RaycastHit
}