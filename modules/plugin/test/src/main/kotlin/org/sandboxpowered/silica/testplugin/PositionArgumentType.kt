package org.sandboxpowered.silica.testplugin

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import org.sandboxpowered.silica.api.util.math.Position

//TODO: Make this more vanilla-like
class PositionArgumentType : ArgumentType<Position> {
    companion object {
        val ERROR_NOT_COMPLETE = SimpleCommandExceptionType(LiteralMessage("The position is not complete"))
    }

    override fun parse(reader: StringReader): Position {
        val cursor = reader.cursor
        val x = reader.readInt()
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip()
            val y = reader.readInt()
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip()
                val z = reader.readInt()
                return Position(x, y, z)
            } else {
                reader.cursor = cursor
                throw ERROR_NOT_COMPLETE.createWithContext(reader)
            }
        } else {
            reader.cursor = cursor
            throw ERROR_NOT_COMPLETE.createWithContext(reader)
        }
    }
}