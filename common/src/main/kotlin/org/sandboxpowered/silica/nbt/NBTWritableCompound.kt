package org.sandboxpowered.silica.nbt

import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.math.Position
import java.util.*

interface NBTWritableCompound : NBT {
    fun setInt(key: String, i: Int)
    fun setIntArray(key: String, i: IntArray)
    fun setLongArray(key: String, i: LongArray)
    fun setString(key: String, s: String)
    fun setFloat(key: String, f: Float)
    fun setDouble(key: String, d: Double)
    fun setByte(key: String, b: Byte)
    fun setByteArray(key: String, b: ByteArray)
    fun setLong(key: String, l: Long)
    fun setBoolean(key: String, bool: Boolean)
    fun setUUID(key: String, uuid: UUID)
    fun setTag(key: String, tag: NBT)
    fun <T : NBT> setList(key: String, list: List<T>)
    fun setPosition(key: String, position: Position)
    fun setIdentifier(key: String, identifier: Identifier)
}