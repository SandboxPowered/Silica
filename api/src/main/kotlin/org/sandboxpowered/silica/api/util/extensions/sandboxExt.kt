package org.sandboxpowered.silica.api.util.extensions

import com.mojang.brigadier.StringReader
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.math.Position

operator fun Position.component1(): Int = x
operator fun Position.component2(): Int = y
operator fun Position.component3(): Int = z

fun Identifier.Companion.read(reader: StringReader): Identifier {
    val i = reader.cursor
    while (reader.canRead() && isAllowedCharacter(reader.peek())) {
        reader.skip()
    }
    val string = reader.string.substring(i, reader.cursor)
    return Identifier(string)
}