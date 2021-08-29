package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class Half(private val s: String) : StringSerializable {
    TOP("top"),
    BOTTOM("bottom"),
    DOUBLE("double");

    override fun getName(): String = s
}