package org.sandboxpowered.silica.api.network

import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.math.Position
import java.util.*

interface PacketBuffer {
    //region Misc
    val readableBytes: Int
    //endregion

    //region Read Primitives
    fun readBoolean(): Boolean
    fun readByte(): Byte
    fun readUnsignedByte(): UByte
    fun readShort(): Short
    fun readUnsignedShort(): UShort
    fun readInt(): Int
    fun readVarInt(): Int
    fun readUnsignedInt(): UInt
    fun readLong(): Long
    fun readVarLong(): Long
    fun readUnsignedLong(): ULong
    fun readFloat(): Float
    fun readDouble(): Double
    fun readChar(): Char
    fun readString(maxSize: Int = 32767): String
    fun readOptionalString(maxSize: Int = 32767): String?
    //endregion

    //region Read Custom
    fun readPosition(): Position
    fun readIdentifier(): Identifier
    fun readUUID(): UUID
    //endregion

    //region Read Arrays
    fun readByteArray(maxSize: Int = readableBytes): ByteArray
    fun readVarIntArray(maxSize: Int = -1): IntArray
    fun readLongArray(maxSize: Int = -1): LongArray
    //endregion

    //region Write Primitives
    fun writeBoolean(value: Boolean): PacketBuffer
    fun writeByte(value: Byte): PacketBuffer
    fun writeUnsignedByte(value: UByte): PacketBuffer
    fun writeShort(value: Short): PacketBuffer
    fun writeUnsignedShort(value: UShort): PacketBuffer
    fun writeInt(value: Int): PacketBuffer
    fun writeVarInt(value: Int): PacketBuffer
    fun writeUnsignedInt(value: UInt): PacketBuffer
    fun writeLong(value: Long): PacketBuffer
    fun writeVarLong(value: Long): PacketBuffer
    fun writeUnsignedLong(value: ULong): PacketBuffer
    fun writeFloat(value: Float): PacketBuffer
    fun writeDouble(value: Double): PacketBuffer
    fun writeChar(value: Char): PacketBuffer
    fun writeString(value: String, maxLength: Int = 32767): PacketBuffer
    fun writeOptionalString(value: String?, maxLength: Int = 32767): PacketBuffer
    //endregion

    //region Write Custom
    fun writePosition(value: Position): PacketBuffer
    fun writeIdentifier(value: Identifier): PacketBuffer
    fun writeUUID(value: UUID): PacketBuffer
    //endregion

    //region Write Arrays
    fun writeByteArray(value: ByteArray): PacketBuffer
    fun writeVarIntArray(value: IntArray): PacketBuffer
    fun writeLongArray(value: LongArray): PacketBuffer
    //endregion
}