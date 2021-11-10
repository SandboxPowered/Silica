package org.sandboxpowered.silica.nbt

import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.byte
import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.byteArray
import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.double
import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.float
import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.int
import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.intArray
import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.long
import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.longArray
import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.string
import org.sandboxpowered.silica.nbt.CompoundTag.Entry.Companion.tag
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.math.Position
import java.util.*

class CompoundTag : NBTCompound {
    internal val tags: MutableMap<String, Entry> = HashMap()

    override fun asString(): String {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = tags.size
    override val keys: Collection<String>
        get() = tags.keys

    override fun contains(key: String) = key in tags

    override fun getInt(key: String): Int = tags[key].int()

    override fun getIntArray(key: String): IntArray = tags[key].intArray()

    override fun getLongArray(key: String): LongArray = tags[key].longArray()

    override fun getString(key: String): String = tags[key].string()

    override fun getDouble(key: String): Double = tags[key].double()

    override fun getByte(key: String): Byte = tags[key].byte()

    override fun getByteArray(key: String): ByteArray = tags[key].byteArray()

    override fun getLong(key: String): Long = tags[key].long()

    override fun getBoolean(key: String) = getByte(key) != 0.toByte()

    override fun getUUID(key: String) = getTag(key).let {
        it as NBTCompound
        UUID(it.getLong("M"), it.getLong("L"))
    }

    override fun remove(key: String) = tags.remove(key) != null

    override fun getTag(key: String): NBT = tags[key].tag()

    override fun <T> getList(key: String, tagType: Class<T>): List<T> {
        TODO("Not yet implemented")
    }

    override fun getCompoundTag(key: String) = tags[key].tag() as? CompoundTag ?: CompoundTag()

    override fun getIdentifier(key: String): Identifier = Identifier(getString(key))

    override fun getPosition(key: String): Position = getTag(key).let {
        it as CompoundTag
        Position(it.getInt("X"), it.getInt("Y"), it.getInt("Z"))
    }

    override fun setInt(key: String, i: Int) {
        tags[key] = int(i)
    }

    override fun setIntArray(key: String, i: IntArray) {
        tags[key] = intArray(i)
    }

    override fun setLongArray(key: String, i: LongArray) {
        tags[key] = longArray(i)
    }

    override fun setString(key: String, s: String) {
        tags[key] = string(s)
    }

    override fun setFloat(key: String, f: Float) {
        tags[key] = float(f)
    }

    override fun setDouble(key: String, d: Double) {
        tags[key] = double(d)
    }

    override fun setByte(key: String, b: Byte) {
        tags[key] = byte(b)
    }

    override fun setByteArray(key: String, b: ByteArray) {
        tags[key] = byteArray(b)
    }

    override fun setLong(key: String, l: Long) {
        tags[key] = long(l)
    }

    override fun setBoolean(key: String, bool: Boolean) {
        tags[key] = byte(if (bool) 1 else 0)
    }

    override fun setUUID(key: String, uuid: UUID) = setTag(key, CompoundTag().apply {
        setLong("M", uuid.mostSignificantBits)
        setLong("L", uuid.leastSignificantBits)
    })

    override fun setTag(key: String, tag: NBT) {
        tags[key] = tag(tag)
    }

    override fun <T : NBT> setList(key: String, list: List<T>) {
        tags[key] = Entry.list(list)
    }

    override fun setPosition(key: String, position: Position) = setTag(key, CompoundTag().apply {
        setInt("X", position.x)
        setInt("Y", position.y)
        setInt("Z", position.z)
    })

    override fun setIdentifier(key: String, identifier: Identifier) = setString(key, identifier.toString())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is CompoundTag) return false

        if (tags.keys != other.tags.keys) return false
        return tags.all { (k, v) -> v == other.tags[k] } // TODO: this doesn't support comparing arrays ;-;
    }

    override fun hashCode(): Int = tags.hashCode() // TODO: probs incorrect

    override fun toString() = "CompoundTag(tags=$tags)"

    internal class Entry private constructor(val type: Int, val value: Any) {
        companion object {
            // 0 = end
            fun byte(value: Byte) = Entry(1, value)
            fun short(value: Short) = Entry(2, value)
            fun int(value: Int) = Entry(3, value)
            fun long(value: Long) = Entry(4, value)
            fun float(value: Float) = Entry(5, value)
            fun double(value: Double) = Entry(6, value)
            fun byteArray(value: ByteArray) = Entry(7, value)
            fun string(value: String) = Entry(8, value)
            fun list(value: List<*>) = Entry(9, value)
            fun tag(value: NBT) = Entry(10, value)
            fun intArray(value: IntArray) = Entry(11, value)
            fun longArray(value: LongArray) = Entry(12, value)
            fun unsafe(type: Int, value: Any) = Entry(type, value)

            fun Entry?.byte(): Byte = if (this == null || type != 1) 0 else value as Byte
            fun Entry?.short(): Short = if (this == null || type != 2) 0 else value as Short
            fun Entry?.int(): Int = if (this == null || type != 3) 0 else value as Int
            fun Entry?.long(): Long = if (this == null || type != 4) 0 else value as Long
            fun Entry?.float(): Float = if (this == null || type != 5) 0f else value as Float
            fun Entry?.double(): Double = if (this == null || type != 6) 0.0 else value as Double
            fun Entry?.byteArray(): ByteArray = if (this == null || type != 7) ByteArray(0) else value as ByteArray
            fun Entry?.string(): String = if (this == null || type != 8) "" else value as String
            fun Entry?.list(): List<*> = if (this == null || type != 9) emptyList<Any>() else value as List<*>
            fun Entry?.tag(): NBT = if (this == null || type != 10) CompoundTag() else value as NBT
            fun Entry?.intArray(): IntArray = if (this == null || type != 11) IntArray(0) else value as IntArray
            fun Entry?.longArray(): LongArray = if (this == null || type != 12) LongArray(0) else value as LongArray
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is Entry) return false

            if (type != other.type) return false
            if (value != other.value) return false

            return true
        }

        override fun hashCode(): Int {
            var result = type
            result = 31 * result + value.hashCode()
            return result
        }

        override fun toString() = "Entry(type=$type, value=$value)"
    }
}