package org.sandboxpowered.silica.vanilla.util

import org.sandboxpowered.silica.api.util.StringSerializable

enum class DoorHalf(override val asString: String) : StringSerializable {
    LOWER("lower"),
    UPPER("upper")
}