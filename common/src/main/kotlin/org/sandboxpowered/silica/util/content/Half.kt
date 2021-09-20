package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class Half(override val asString: String) : StringSerializable {
    TOP("top"),
    BOTTOM("bottom"),
    DOUBLE("double");
}