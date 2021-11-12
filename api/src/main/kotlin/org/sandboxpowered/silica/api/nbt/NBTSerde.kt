package org.sandboxpowered.silica.api.nbt

import org.sandboxpowered.silica.api.nbt.CompoundTag.Entry
import java.io.DataInput
import java.io.DataOutput
import java.io.IOException

/**
 * Reads a [NBTCompound] from this [DataInput]
 */
fun DataInput.readNbt(): NBTCompound {
    return when (val type = readByte().toInt()) {
        0 -> CompoundTag()
        else -> {
            readUTF()
            NbtType.read(type, this).value as? NBTCompound ?: throw IOException("Root must be a named compound tag")
        }
    }
}

/**
 * Writes a [NBTCompound] to this [DataOutput]
 */
fun DataOutput.write(tag: NBTCompound) {
    writeByte(NbtType.TAG.vanillaId)
    writeUTF("")
    NbtType.TAG.write(this, tag)
}

// TODO: Vanilla has a total read size limit, as well as a max recursive depth. This would make sense to add for security
private enum class NbtType(
    val read: DataInput.() -> Any,
    val write: DataOutput.(Any) -> Unit,
) {
    BYTE(DataInput::readByte, { writeByte((it as Byte).toInt()) }),
    SHORT(DataInput::readShort, { writeShort((it as Short).toInt()) }),
    INT(DataInput::readInt, { writeInt(it as Int) }),
    LONG(DataInput::readLong, { writeLong(it as Long) }),
    FLOAT(DataInput::readFloat, { writeFloat(it as Float) }),
    DOUBLE(DataInput::readDouble, { writeDouble(it as Double) }),
    BYTE_ARRAY({
        val s = readInt()
        ByteArray(s).also(this::readFully)
    }, {
        it as ByteArray
        writeInt(it.size)
        write(it)
    }),
    STRING(DataInput::readUTF, { writeUTF(it as String) }),
    LIST({
        val type = readByte()
        val s = readInt()
        List(s) {
            read(type.toInt(), this)
        }
    }, {
        it as List<CompoundTag>
        val type = if (it.isEmpty()) {
            0
        } else {
            TAG.vanillaId
        }

        writeByte(type)
        writeInt(it.size)

        it.forEach { entry ->
            getNbtType(TAG.vanillaId).write(this, entry)
        }
    }),
    TAG({
        val compound = CompoundTag()
        var type = readByte().toInt()
        while (type != 0) {
            val k = readUTF()
            val v = read(type, this)
            compound.tags[k] = v
            type = readByte().toInt()
        }
        compound
    }, {
        it as CompoundTag
        for ((k, v) in it.tags) write(k, v, this)
        writeByte(0)
    }),
    INT_ARRAY({
        val s = readInt()
        IntArray(s) { readInt() }
    }, {
        it as IntArray
        writeInt(it.size)
        it.forEach(this::writeInt)
    }),
    LONG_ARRAY({
        val s = readInt()
        LongArray(s) { readLong() }
    }, {
        it as LongArray
        writeInt(it.size)
        it.forEach(this::writeLong)
    });

    val vanillaId get() = ordinal + 1

    companion object {
        fun read(type: Int, input: DataInput): Entry =
            Entry.unsafe(type, getNbtType(type).read(input))

        fun write(key: String, what: Entry, output: DataOutput) {
            output.writeByte(what.type)
            output.writeUTF(key)
            getNbtType(what.type).write(output, what.value)
        }

        private fun getNbtType(type: Int) =
            values().getOrElse(type - 1) { throw IllegalArgumentException("Type $type is invalid") }
    }
}