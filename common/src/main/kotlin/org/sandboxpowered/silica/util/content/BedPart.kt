package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class BedPart(override val asString: String) : StringSerializable {
    HEAD("head"),
    FOOT("foot")
}