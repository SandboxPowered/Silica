package org.sandboxpowered.silica.api.nbt

import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.math.Position
import java.util.*

interface NBTWritableCompound : NBT {
    fun setInt(key: String, i: Int)
    infix fun String.to(i: Int) = setInt(this, i)

    fun setIntArray(key: String, i: IntArray)
    infix fun String.to(i: IntArray) = setIntArray(this, i)

    fun setLongArray(key: String, i: LongArray)
    infix fun String.to(i: LongArray) = setLongArray(this, i)

    fun setString(key: String, s: String)
    infix fun String.to(s: String) = setString(this, s)

    fun setFloat(key: String, f: Float)
    infix fun String.to(f: Float) = setFloat(this, f)

    fun setDouble(key: String, d: Double)
    infix fun String.to(d: Double) = setDouble(this, d)

    fun setByte(key: String, b: Byte)
    infix fun String.to(b: Byte) = setByte(this, b)

    fun setByteArray(key: String, b: ByteArray)
    infix fun String.to(b: ByteArray) = setByteArray(this, b)

    fun setLong(key: String, l: Long)
    infix fun String.to(l: Long) = setLong(this, l)

    fun setBoolean(key: String, bool: Boolean)
    infix fun String.to(bool: Boolean) = setBoolean(this, bool)

    fun setUUID(key: String, uuid: UUID)
    infix fun String.to(uuid: UUID) = setUUID(this, uuid)

    fun setTag(key: String, tag: NBT)
    infix fun String.to(tag: NBT) = setTag(this, tag)

    fun <T : NBT> setList(key: String, list: List<T>)
    infix fun <T : NBT> String.to(list: List<T>) = setList(this, list)

    fun setPosition(key: String, position: Position)
    infix fun String.to(position: Position) = setPosition(this, position)

    fun setIdentifier(key: String, identifier: Identifier)
    infix fun String.to(identifier: Identifier) = setIdentifier(this, identifier)
}