package org.sandboxpowered.silica.api.network

import net.kyori.adventure.text.Component
import org.joml.Vector3f
import org.sandboxpowered.silica.api.nbt.NBTCompound
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.math.Position
import java.util.*

interface PacketBuffer {
    companion object {
        fun getVarIntSize(i: Int): Int {
            for (j in 1..4) {
                if (i and -1 shl j * 7 == 0) {
                    return j
                }
            }
            return 5
        }
    }

    //region Misc
    val readableBytes: Int
    //endregion

    //region Read Primitives
    fun readBoolean(): Boolean
    fun readByte(): Byte
    fun readUByte(): UByte
    fun readShort(): Short
    fun readUShort(): UShort
    fun readInt(): Int
    fun readVarInt(): Int
    fun readUInt(): UInt
    fun readLong(): Long
    fun readVarLong(): Long
    fun readULong(): ULong
    fun readFloat(): Float
    fun readDouble(): Double
    fun readString(maxSize: Int = 32767): String
    fun readOptionalString(maxSize: Int = 32767): String?
    //endregion

    //region Read Custom
    fun readPosition(): Position
    fun readIdentifier(): Identifier
    fun readUUID(): UUID
    fun readText(): Component
    fun readNBT(): NBTCompound?
    //endregion

    //region Read JOML
    fun readVector3f(): Vector3f
    //endregion

    //region Read Arrays
    fun readByteArray(maxSize: Int = readableBytes): ByteArray
    fun readVarIntArray(maxSize: Int = -1): IntArray
    fun readLongArray(maxSize: Int = -1): LongArray
    fun readBytes(maxSize: Int): PacketBuffer
    fun readSlice(maxSize: Int): PacketBuffer
    //endregion

    //region Write Primitives
    fun writeBoolean(value: Boolean): PacketBuffer
    fun writeByte(value: Byte): PacketBuffer
    fun writeUByte(value: UByte): PacketBuffer
    fun writeShort(value: Short): PacketBuffer
    fun writeUShort(value: UShort): PacketBuffer
    fun writeInt(value: Int): PacketBuffer
    fun writeVarInt(value: Int): PacketBuffer
    fun writeUInt(value: UInt): PacketBuffer
    fun writeLong(value: Long): PacketBuffer
    fun writeVarLong(value: Long): PacketBuffer
    fun writeULong(value: ULong): PacketBuffer
    fun writeFloat(value: Float): PacketBuffer
    fun writeDouble(value: Double): PacketBuffer
    fun writeString(value: String, maxLength: Int = 32767): PacketBuffer
    fun writeOptionalString(value: String?, maxLength: Int = 32767): PacketBuffer
    //endregion

    //region Write Custom
    fun writePosition(value: Position): PacketBuffer
    fun writeIdentifier(value: Identifier): PacketBuffer
    fun writeUUID(value: UUID): PacketBuffer
    fun writeText(value: Component): PacketBuffer
    fun writeNBT(value: NBTCompound?): PacketBuffer
    //endregion

    //region Write JOML
    fun writeVector3f(value: Vector3f): PacketBuffer
    fun writeVector3f(x: Float, y: Float, z: Float): PacketBuffer
    //endregion

    //region Write Arrays
    fun writeByteArray(value: ByteArray): PacketBuffer
    fun writeVarIntArray(value: IntArray): PacketBuffer
    fun writeLongArray(value: LongArray): PacketBuffer
    fun writeBytes(value: ByteArray): PacketBuffer
    fun writeBytes(value: PacketBuffer): PacketBuffer

    //endregion
}

//region Extensions
@JvmOverloads
inline fun <reified T : Enum<T>> PacketBuffer.readEnum(transform: PacketBuffer.() -> Int = PacketBuffer::readVarInt): T {
    return T::class.java.enumConstants[transform(this)]
}

fun <T : Enum<T>> PacketBuffer.writeEnum(
    value: T,
    transform: PacketBuffer.(Int) -> PacketBuffer = PacketBuffer::writeVarInt
): PacketBuffer {
    return transform(this, value.ordinal)
}

inline fun <T> PacketBuffer.readCollection(transform: (PacketBuffer) -> T): Collection<T> =
    readCollection(-1, transform)

inline fun <T> PacketBuffer.readCollection(maxSize: Int, transform: (PacketBuffer) -> T): Collection<T> {
    val size = readVarInt()
    require(maxSize == -1 || size <= maxSize) { "Read collection size was $size, expected max $maxSize" }
    return List(size) { transform(this) }
}

inline fun <T> PacketBuffer.writeCollection(
    value: Collection<T>,
    transform: PacketBuffer.(T) -> PacketBuffer
): PacketBuffer {
    writeVarInt(value.size)
    return value.fold(this, transform)
}
//endregion