package org.sandboxpowered.silica.testplugin

import com.mojang.brigadier.LiteralMessage
import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType
import org.joml.Vector3d

//TODO: Make this more vanilla-like
class Vec3dArgumentType : ArgumentType<Vector3d> {
    companion object {
        val ERROR_NOT_COMPLETE = SimpleCommandExceptionType(LiteralMessage("The position is not complete"))
    }

    override fun parse(reader: StringReader): Vector3d {
        val cursor = reader.cursor
        val x = reader.readDouble()
        if (reader.canRead() && reader.peek() == ' ') {
            reader.skip()
            val y = reader.readDouble()
            if (reader.canRead() && reader.peek() == ' ') {
                reader.skip()
                val z = reader.readDouble()
                return Vector3d(x, y, z)
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