package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.api.util.StringSerializable

enum class WallShape(override val asString: String) : StringSerializable {
    NONE("none"),
    LOW("low"),
    TALL("tall");
}