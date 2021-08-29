package org.sandboxpowered.silica.util

import org.sandboxpowered.silica.state.property.StringSerializable

enum class StairShape(private val n: String) : StringSerializable {
    OUTER_LEFT("outer_left"),
    OUTER_RIGHT("outer_right"),
    INNER_LEFT("inner_left"),
    INNER_RIGHT("inner_right"),
    STRAIGHT("straight")
    ;

    override fun getName(): String = n
}