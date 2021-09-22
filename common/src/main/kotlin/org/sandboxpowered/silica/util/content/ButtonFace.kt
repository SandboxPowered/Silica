package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.world.state.property.StringSerializable

enum class ButtonFace(override val asString: String) : StringSerializable {
    FLOOR("floor"),
    CEILING("ceiling"),
    WALL("wall"),
}