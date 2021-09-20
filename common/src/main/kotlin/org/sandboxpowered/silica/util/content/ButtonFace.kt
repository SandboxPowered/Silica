package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class ButtonFace(override val asString: String) : StringSerializable {
    FLOOR("floor"),
    CEILING("ceiling"),
    WALL("wall"),
}