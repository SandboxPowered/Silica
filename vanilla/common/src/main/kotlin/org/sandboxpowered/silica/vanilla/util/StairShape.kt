package org.sandboxpowered.silica.vanilla.util

import org.sandboxpowered.silica.api.util.StringSerializable

enum class StairShape(override val asString: String) : StringSerializable {
    OUTER_LEFT("outer_left"),
    OUTER_RIGHT("outer_right"),
    INNER_LEFT("inner_left"),
    INNER_RIGHT("inner_right"),
    STRAIGHT("straight");
}