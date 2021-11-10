package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.api.util.StringSerializable

enum class DoorHinge(override val asString: String) : StringSerializable {
    LEFT("left"),
    RIGHT("right")
}