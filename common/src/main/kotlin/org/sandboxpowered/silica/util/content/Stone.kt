package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class Stone(override val asString: String) : StringSerializable {
    STONE("stone"),
    DIORITE("diorite"),
    GRANITE("granite"),
    ANDESITE("andesite");

    companion object {
        val ALL = values()
        val NAMES = ALL.map { it.asString }
    }
}