package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class Stone(val string: String) : StringSerializable {
    STONE("stone"),
    DIORITE("diorite"),
    GRANITE("granite"),
    ANDESITE("andesite");

    override fun getName(): String = string
}