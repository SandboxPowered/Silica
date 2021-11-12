package org.sandboxpowered.silica.api.entity

import org.joml.Vector3f
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.Hand
import org.sandboxpowered.silica.api.util.math.Position

data class InteractionContext(
    val hand: Hand,
    val location: Position,
    val face: Direction,
    val cursor: Vector3f,
    val insideBlock: Boolean
)