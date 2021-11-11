package org.sandboxpowered.silica.vanilla.util

import org.sandboxpowered.silica.api.util.StringSerializable

enum class BedPart(override val asString: String) : StringSerializable {
    HEAD("head"),
    FOOT("foot")
}