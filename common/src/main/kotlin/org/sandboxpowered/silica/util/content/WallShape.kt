package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.world.state.property.StringSerializable

enum class WallShape(override val asString: String) : StringSerializable {
    NONE("none"),
    LOW("low"),
    TALL("tall");
}