package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class Stone(override val asString: String) : StringSerializable {
    COBBLESTONE("cobblestone"),
    STONE("stone"),
    DIORITE("diorite"),
    POLISHED_DIORITE("polished_diorite"),
    GRANITE("granite"),
    POLISHED_GRANITE("polished_granite"),
    ANDESITE("andesite"),
    POLISHED_ANDESITE("polished_andesite");

    companion object {
        val ALL = values()
        val NAMES = ALL.map { it.asString }
    }
}