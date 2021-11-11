package org.sandboxpowered.silica.vanilla.util

import org.sandboxpowered.silica.api.util.StringSerializable

enum class RedstoneSideState(override val asString: String) : StringSerializable {
    NONE("none"),
    SIDE("side"),
    UP("up");
}