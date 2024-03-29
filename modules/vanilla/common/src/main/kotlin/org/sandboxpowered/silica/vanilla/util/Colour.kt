package org.sandboxpowered.silica.vanilla.util

import org.sandboxpowered.silica.api.util.StringSerializable

enum class Colour(override val asString: String) : StringSerializable {
    BLACK("black"),
    RED("red"),
    GREEN("green"),
    BROWN("brown"),
    BLUE("blue"),
    PURPLE("purple"),
    CYAN("cyan"),
    LIGHT_GRAY("light_gray"),
    GRAY("gray"),
    PINK("pink"),
    LIME("lime"),
    YELLOW("yellow"),
    LIGHT_BLUE("light_blue"),
    MAGENTA("magenta"),
    ORANGE("orange"),
    WHITE("white");

    companion object {
        val ALL = values()
        val NAMES = ALL.map { it.asString }
        val NAME_ARRAY = NAMES.toTypedArray()
    }
}