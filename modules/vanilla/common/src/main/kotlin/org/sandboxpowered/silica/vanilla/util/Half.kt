package org.sandboxpowered.silica.vanilla.util

import org.sandboxpowered.silica.api.util.StringSerializable

enum class Half(override val asString: String) : StringSerializable {
    TOP("top"),
    BOTTOM("bottom"),
    DOUBLE("double");
}