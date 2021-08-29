package org.sandboxpowered.silica.util.content

import org.sandboxpowered.silica.state.property.StringSerializable

enum class Colour(private val s: String) : StringSerializable {
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
    WHITE("white")
    ;

    override fun getName(): String = s

    companion object {
        val ALL = values()
        val NAMES = ALL.map { it.getName() }
    }
}