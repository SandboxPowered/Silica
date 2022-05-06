package org.sandboxpowered.silica.testplugin

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import org.joml.Vector3f

//TODO: Make this more vanilla-like
class Vec3fArgumentType : ArgumentType<Vector3f> {
    companion object {
        val ERROR_NOT_COMPLETE = SimpleCommandExceptionType(LiteralMessage("The position is not complete"))
    }

    override fun parse(reader: StringReader): Vector3f {
        val cursor = reader.cursor
        val x = reader.readFloat()
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip()
            val y = reader.readFloat()
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip()
                val z = reader.readFloat()
                return Vector3f(x, y, z)
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