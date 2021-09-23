package org.sandboxpowered.silica.vanilla.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import io.netty.util.ByteProcessor
import org.sandboxpowered.silica.content.item.ItemStack
import org.sandboxpowered.silica.nbt.NBTCompound
import org.sandboxpowered.silica.nbt.readNbt
import org.sandboxpowered.silica.nbt.write
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.math.Position
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.nio.channels.GatheringByteChannel
import java.nio.channels.ScatteringByteChannel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets
import java.util.*
import kotlin.experimental.and

class PacketByteBuf(private val source: ByteBuf) : ByteBuf() {
    override fun capacity(): Int = source.capacity()

    override fun capacity(i: Int): ByteBuf = source.capacity(i)

    override fun maxCapacity(): Int = source.maxCapacity()

    override fun alloc(): ByteBufAllocator = source.alloc()

    override fun order(): ByteOrder = source.order()

    override fun order(byteOrder: ByteOrder): ByteBuf = source.order(byteOrder)

    override fun unwrap(): ByteBuf = source.unwrap()

    override fun isDirect(): Boolean = source.isDirect

    override fun isReadOnly(): Boolean = source.isReadOnly

    override fun asReadOnly(): ByteBuf = source.asReadOnly()

    override fun readerIndex(): Int = source.readerIndex()

    override fun readerIndex(i: Int): ByteBuf = source.readerIndex(i)

    override fun writerIndex(): Int = source.writerIndex()

    override fun writerIndex(i: Int): ByteBuf = source.writerIndex(i)

    override fun setIndex(i: Int, j: Int): ByteBuf = source.setIndex(i, j)

    override fun readableBytes(): Int = source.readableBytes()

    override fun writableBytes(): Int = source.writableBytes()

    override fun maxWritableBytes(): Int = source.maxWritableBytes()

    override fun isReadable(): Boolean = source.isReadable

    override fun isReadable(i: Int): Boolean = source.isReadable(i)

    override fun isWritable(): Boolean = source.isWritable

    override fun isWritable(i: Int): Boolean = source.isWritable(i)

    override fun clear(): ByteBuf = source.clear()

    override fun markReaderIndex(): ByteBuf = source.markReaderIndex()

    override fun resetReaderIndex(): ByteBuf = source.resetReaderIndex()

    override fun markWriterIndex(): ByteBuf = source.markWriterIndex()

    override fun resetWriterIndex(): ByteBuf = source.resetWriterIndex()

    override fun discardReadBytes(): ByteBuf = source.discardReadBytes()

    override fun discardSomeReadBytes(): ByteBuf = source.discardSomeReadBytes()

    override fun ensureWritable(i: Int): ByteBuf = source.ensureWritable(i)

    override fun ensureWritable(i: Int, bl: Boolean): Int = source.ensureWritable(i, bl)

    override fun getBoolean(i: Int): Boolean = source.getBoolean(i)

    override fun getByte(i: Int): Byte = source.getByte(i)

    override fun getUnsignedByte(i: Int): Short = source.getUnsignedByte(i)

    override fun getShort(i: Int): Short = source.getShort(i)

    override fun getShortLE(i: Int): Short = source.getShortLE(i)

    override fun getUnsignedShort(i: Int): Int = source.getUnsignedShort(i)

    override fun getUnsignedShortLE(i: Int): Int = source.getUnsignedShortLE(i)

    override fun getMedium(i: Int): Int = source.getMedium(i)

    override fun getMediumLE(i: Int): Int = source.getMediumLE(i)

    override fun getUnsignedMedium(i: Int): Int = source.getUnsignedMedium(i)

    override fun getUnsignedMediumLE(i: Int): Int = source.getUnsignedMediumLE(i)

    override fun getInt(i: Int): Int = source.getInt(i)

    override fun getIntLE(i: Int): Int = source.getIntLE(i)

    override fun getUnsignedInt(i: Int): Long = source.getUnsignedInt(i)

    override fun getUnsignedIntLE(i: Int): Long = source.getUnsignedIntLE(i)

    override fun getLong(i: Int): Long = source.getLong(i)

    override fun getLongLE(i: Int): Long = source.getLongLE(i)

    override fun getChar(i: Int): Char = source.getChar(i)

    override fun getFloat(i: Int): Float = source.getFloat(i)

    override fun getDouble(i: Int): Double = source.getDouble(i)

    override fun getBytes(i: Int, byteBuf: ByteBuf): ByteBuf = source.getBytes(i, byteBuf)

    override fun getBytes(i: Int, byteBuf: ByteBuf, j: Int): ByteBuf = source.getBytes(i, byteBuf, j)

    override fun getBytes(i: Int, byteBuf: ByteBuf, j: Int, k: Int): ByteBuf = source.getBytes(i, byteBuf, j, k)

    override fun getBytes(i: Int, bs: ByteArray): ByteBuf = source.getBytes(i, bs)

    override fun getBytes(i: Int, bs: ByteArray, j: Int, k: Int): ByteBuf = source.getBytes(i, bs, j, k)

    override fun getBytes(i: Int, byteBuffer: ByteBuffer): ByteBuf = source.getBytes(i, byteBuffer)

    @Throws(IOException::class)
    override fun getBytes(i: Int, outputStream: OutputStream, j: Int): ByteBuf = source.getBytes(i, outputStream, j)

    @Throws(IOException::class)
    override fun getBytes(i: Int, bc: GatheringByteChannel, j: Int): Int = source.getBytes(i, bc, j)

    @Throws(IOException::class)
    override fun getBytes(i: Int, fc: FileChannel, l: Long, j: Int): Int = source.getBytes(i, fc, l, j)

    override fun getCharSequence(i: Int, j: Int, charset: Charset): CharSequence = source.getCharSequence(i, j, charset)

    override fun setBoolean(i: Int, bl: Boolean): ByteBuf = source.setBoolean(i, bl)

    override fun setByte(i: Int, j: Int): ByteBuf = source.setByte(i, j)

    override fun setShort(i: Int, j: Int): ByteBuf = source.setShort(i, j)

    override fun setShortLE(i: Int, j: Int): ByteBuf = source.setShortLE(i, j)

    override fun setMedium(i: Int, j: Int): ByteBuf = source.setMedium(i, j)

    override fun setMediumLE(i: Int, j: Int): ByteBuf = source.setMediumLE(i, j)

    override fun setInt(i: Int, j: Int): ByteBuf = source.setInt(i, j)

    override fun setIntLE(i: Int, j: Int): ByteBuf = source.setIntLE(i, j)

    override fun setLong(i: Int, l: Long): ByteBuf = source.setLong(i, l)

    override fun setLongLE(i: Int, l: Long): ByteBuf = source.setLongLE(i, l)

    override fun setChar(i: Int, j: Int): ByteBuf = source.setChar(i, j)

    override fun setFloat(i: Int, f: Float): ByteBuf = source.setFloat(i, f)

    override fun setDouble(i: Int, d: Double): ByteBuf = source.setDouble(i, d)

    override fun setBytes(i: Int, byteBuf: ByteBuf): ByteBuf = source.setBytes(i, byteBuf)

    override fun setBytes(i: Int, byteBuf: ByteBuf, j: Int): ByteBuf = source.setBytes(i, byteBuf, j)

    override fun setBytes(i: Int, byteBuf: ByteBuf, j: Int, k: Int): ByteBuf = source.setBytes(i, byteBuf, j, k)

    override fun setBytes(i: Int, bs: ByteArray): ByteBuf = source.setBytes(i, bs)

    override fun setBytes(i: Int, bs: ByteArray, j: Int, k: Int): ByteBuf = source.setBytes(i, bs, j, k)

    override fun setBytes(i: Int, byteBuffer: ByteBuffer): ByteBuf = source.setBytes(i, byteBuffer)

    @Throws(IOException::class)
    override fun setBytes(i: Int, inputStream: InputStream, j: Int): Int = source.setBytes(i, inputStream, j)

    @Throws(IOException::class)
    override fun setBytes(i: Int, sbc: ScatteringByteChannel, j: Int): Int = source.setBytes(i, sbc, j)

    @Throws(IOException::class)
    override fun setBytes(i: Int, fc: FileChannel, l: Long, j: Int): Int = source.setBytes(i, fc, l, j)

    override fun setZero(i: Int, j: Int): ByteBuf = source.setZero(i, j)

    override fun setCharSequence(i: Int, charSequence: CharSequence, charset: Charset): Int =
        source.setCharSequence(i, charSequence, charset)

    override fun readBoolean(): Boolean = source.readBoolean()

    override fun readByte(): Byte = source.readByte()

    override fun readUnsignedByte(): Short = source.readUnsignedByte()

    override fun readShort(): Short = source.readShort()

    override fun readShortLE(): Short = source.readShortLE()

    override fun readUnsignedShort(): Int = source.readUnsignedShort()

    fun readUByte(): UByte = readUnsignedByte().toUByte()

    fun writeUByte(b: UByte): ByteBuf = writeByte(b.toInt())

    fun readUShort(): UShort = readUnsignedShort().toUShort()

    fun writeUShort(s: UShort): ByteBuf = writeShort(s.toInt())

    fun readUInt(): UInt = readVarInt().toUInt()

    fun writeUInt(i: UInt): ByteBuf = writeVarInt(i.toInt())

    fun readULong(): ULong = readVarLong().toULong()

    fun writeULong(l: ULong): ByteBuf = writeVarLong(l.toLong())

    override fun readUnsignedShortLE(): Int = source.readUnsignedShortLE()

    override fun readMedium(): Int = source.readMedium()

    override fun readMediumLE(): Int = source.readMediumLE()

    override fun readUnsignedMedium(): Int = source.readUnsignedMedium()

    override fun readUnsignedMediumLE(): Int = source.readUnsignedMediumLE()

    override fun readInt(): Int = source.readInt()

    override fun readIntLE(): Int = source.readIntLE()

    override fun readUnsignedInt(): Long = source.readUnsignedInt()

    override fun readUnsignedIntLE(): Long = source.readUnsignedIntLE()

    override fun readLong(): Long = source.readLong()

    override fun readLongLE(): Long = source.readLongLE()

    override fun readChar(): Char = source.readChar()

    override fun readFloat(): Float = source.readFloat()

    override fun readDouble(): Double = source.readDouble()

    override fun readBytes(i: Int): ByteBuf = source.readBytes(i)

    override fun readSlice(i: Int): ByteBuf = source.readSlice(i)

    override fun readRetainedSlice(i: Int): ByteBuf = source.readRetainedSlice(i)

    override fun readBytes(byteBuf: ByteBuf): ByteBuf = source.readBytes(byteBuf)

    override fun readBytes(byteBuf: ByteBuf, i: Int): ByteBuf = source.readBytes(byteBuf, i)

    override fun readBytes(byteBuf: ByteBuf, i: Int, j: Int): ByteBuf = source.readBytes(byteBuf, i, j)

    override fun readBytes(bs: ByteArray): ByteBuf = source.readBytes(bs)

    override fun readBytes(bs: ByteArray, i: Int, j: Int): ByteBuf = source.readBytes(bs, i, j)

    override fun readBytes(byteBuffer: ByteBuffer): ByteBuf = source.readBytes(byteBuffer)

    @Throws(IOException::class)
    override fun readBytes(outputStream: OutputStream, i: Int): ByteBuf = source.readBytes(outputStream, i)

    @Throws(IOException::class)
    override fun readBytes(gatheringByteChannel: GatheringByteChannel, i: Int): Int =
        source.readBytes(gatheringByteChannel, i)

    override fun readCharSequence(i: Int, charset: Charset): CharSequence = source.readCharSequence(i, charset)

    @Throws(IOException::class)
    override fun readBytes(fileChannel: FileChannel, l: Long, i: Int): Int = source.readBytes(fileChannel, l, i)

    override fun skipBytes(i: Int): ByteBuf = source.skipBytes(i)

    override fun writeBoolean(bl: Boolean): ByteBuf = source.writeBoolean(bl)

    override fun writeByte(i: Int): ByteBuf = source.writeByte(i)

    fun writeByte(b: Byte): ByteBuf = source.writeByte(b.toInt())

    override fun writeShort(i: Int): ByteBuf = source.writeShort(i)
    fun writeShort(s: Short): ByteBuf = source.writeShort(s.toInt())

    override fun writeShortLE(i: Int): ByteBuf = source.writeShortLE(i)

    override fun writeMedium(i: Int): ByteBuf = source.writeMedium(i)

    override fun writeMediumLE(i: Int): ByteBuf = source.writeMediumLE(i)

    override fun writeInt(i: Int): ByteBuf = source.writeInt(i)

    override fun writeIntLE(i: Int): ByteBuf = source.writeIntLE(i)

    override fun writeLong(l: Long): ByteBuf = source.writeLong(l)

    override fun writeLongLE(l: Long): ByteBuf = source.writeLongLE(l)

    override fun writeChar(i: Int): ByteBuf = source.writeChar(i)

    override fun writeFloat(f: Float): ByteBuf = source.writeFloat(f)

    override fun writeDouble(d: Double): ByteBuf = source.writeDouble(d)

    override fun writeBytes(byteBuf: ByteBuf): ByteBuf = source.writeBytes(byteBuf)

    override fun writeBytes(byteBuf: ByteBuf, i: Int): ByteBuf = source.writeBytes(byteBuf, i)

    override fun writeBytes(byteBuf: ByteBuf, i: Int, j: Int): ByteBuf = source.writeBytes(byteBuf, i, j)

    override fun writeBytes(bs: ByteArray): ByteBuf = source.writeBytes(bs)

    override fun writeBytes(bs: ByteArray, i: Int, j: Int): ByteBuf = source.writeBytes(bs, i, j)

    override fun writeBytes(byteBuffer: ByteBuffer): ByteBuf = source.writeBytes(byteBuffer)

    @Throws(IOException::class)
    override fun writeBytes(inputStream: InputStream, i: Int): Int = source.writeBytes(inputStream, i)

    @Throws(IOException::class)
    override fun writeBytes(bc: ScatteringByteChannel, i: Int): Int = source.writeBytes(bc, i)

    @Throws(IOException::class)
    override fun writeBytes(fc: FileChannel, l: Long, i: Int): Int = source.writeBytes(fc, l, i)

    override fun writeZero(i: Int): ByteBuf = source.writeZero(i)

    override fun writeCharSequence(cs: CharSequence, charset: Charset): Int = source.writeCharSequence(cs, charset)

    override fun indexOf(i: Int, j: Int, b: Byte): Int = source.indexOf(i, j, b)

    override fun bytesBefore(b: Byte): Int = source.bytesBefore(b)

    override fun bytesBefore(i: Int, b: Byte): Int = source.bytesBefore(i, b)

    override fun bytesBefore(i: Int, j: Int, b: Byte): Int = source.bytesBefore(i, j, b)

    override fun forEachByte(byteProcessor: ByteProcessor): Int = source.forEachByte(byteProcessor)

    override fun forEachByte(i: Int, j: Int, bp: ByteProcessor): Int = source.forEachByte(i, j, bp)

    override fun forEachByteDesc(bp: ByteProcessor): Int = source.forEachByteDesc(bp)

    override fun forEachByteDesc(i: Int, j: Int, bp: ByteProcessor): Int = source.forEachByteDesc(i, j, bp)

    override fun copy(): ByteBuf = source.copy()

    override fun copy(i: Int, j: Int): ByteBuf = source.copy(i, j)

    override fun slice(): ByteBuf = source.slice()

    override fun retainedSlice(): ByteBuf = source.retainedSlice()

    override fun slice(i: Int, j: Int): ByteBuf = source.slice(i, j)

    override fun retainedSlice(i: Int, j: Int): ByteBuf = source.retainedSlice(i, j)

    override fun duplicate(): ByteBuf = source.duplicate()

    override fun retainedDuplicate(): ByteBuf = source.retainedDuplicate()

    override fun nioBufferCount(): Int = source.nioBufferCount()

    override fun nioBuffer(): ByteBuffer = source.nioBuffer()

    override fun nioBuffer(i: Int, j: Int): ByteBuffer = source.nioBuffer(i, j)

    override fun internalNioBuffer(i: Int, j: Int): ByteBuffer = source.internalNioBuffer(i, j)

    override fun nioBuffers(): Array<ByteBuffer> = source.nioBuffers()

    override fun nioBuffers(i: Int, j: Int): Array<ByteBuffer> = source.nioBuffers(i, j)

    override fun hasArray(): Boolean = source.hasArray()

    override fun array(): ByteArray = source.array()

    override fun arrayOffset(): Int = source.arrayOffset()

    override fun hasMemoryAddress(): Boolean = source.hasMemoryAddress()

    override fun memoryAddress(): Long = source.memoryAddress()

    override fun toString(charset: Charset): String = source.toString(charset)

    override fun toString(i: Int, j: Int, charset: Charset): String = source.toString(i, j, charset)

    override fun hashCode(): Int = source.hashCode()

    override fun equals(other: Any?): Boolean = source == other

    override fun compareTo(other: ByteBuf): Int = source.compareTo(other)

    override fun toString(): String = source.toString()

    override fun retain(i: Int): ByteBuf = source.retain(i)

    override fun retain(): ByteBuf = source.retain()

    override fun touch(): ByteBuf = source.touch()

    override fun touch(o: Any): ByteBuf = source.touch(o)

    override fun refCnt(): Int = source.refCnt()

    override fun release(): Boolean = source.release()

    override fun release(i: Int): Boolean = source.release(i)

    fun readVarInt(): Int {
        var i = 0
        var j = 0

        var b: Byte
        do {
            b = readByte()
            i = i or ((b and 127).toInt() shl j++ * 7)
            if (j > 5) {
                throw RuntimeException("VarInt too big")
            }
        } while (b.toInt() and 128 == 128)

        return i
    }

    fun readVarLong(): Long {
        var l = 0L
        var i = 0
        var b: Int
        do {
            b = readByte().toInt()
            l = l or ((b and 127).toLong() shl i++ * 7)
            if (i > 10) {
                throw RuntimeException("VarLong too big")
            }
        } while (b and 128 == 128)
        return l
    }

    @JvmOverloads
    fun readString(i: Int = 32767): String {
        val j = readVarInt()
        return if (j > i * 4) {
            throw DecoderException("The received encoded string buffer length is longer than maximum allowed (" + j + " > " + i * 4 + ")")
        } else if (j < 0) {
            throw DecoderException("The received encoded string buffer length is less than zero! Weird string!")
        } else {
            val string = this.toString(this.readerIndex(), j, StandardCharsets.UTF_8)
            this.readerIndex(this.readerIndex() + j)
            if (string.length > i) {
                throw DecoderException("The received string length is longer than maximum allowed ($j > $i)")
            } else {
                string
            }
        }
    }

    fun writeVarInt(i: Int): ByteBuf {
        var ii = i
        while (ii and -128 != 0) {
            writeByte(ii and 127 or 128)
            ii = ii ushr 7
        }
        writeByte(ii)
        return this
    }

    fun writeVarIntArray(array: IntArray): ByteBuf {
        writeVarInt(array.size)
        array.forEach(this::writeVarInt)
        return this
    }

    fun writeLongArray(array: LongArray): ByteBuf {
        writeVarInt(array.size)
        array.forEach(this::writeLong)
        return this
    }

    @JvmOverloads
    fun writeString(string: String, i: Int = 32767): ByteBuf {
        val bs = string.toByteArray(StandardCharsets.UTF_8)
        return if (bs.size > i) {
            throw EncoderException("String too big (was " + bs.size + " bytes encoded, max " + i + ")")
        } else {
            writeVarInt(bs.size)
            this.writeBytes(bs)
            this
        }
    }

    @JvmOverloads
    fun readByteArray(maxSize: Int = readableBytes()): ByteArray {
        val length = readVarInt()
        return if (length > maxSize) {
            throw DecoderException("ByteArray with size $length is bigger than allowed $maxSize")
        } else {
            val bs = ByteArray(length)
            this.readBytes(bs)
            bs
        }
    }

    fun writeByteArray(array: ByteArray): ByteBuf {
        writeVarInt(array.size)
        this.writeBytes(array)
        return this
    }

    fun readUUID(): UUID = UUID(readLong(), readLong())

    fun writeUUID(uuid: UUID): ByteBuf {
        writeLong(uuid.mostSignificantBits)
        writeLong(uuid.leastSignificantBits)
        return this
    }

    fun readIdentityArray(): Array<Identifier> {
        val size = readVarInt()
        val arr = arrayOfNulls<Identifier>(size)
        for (i in 0 until size) {
            arr[i] = readIdentity()
        }
        return arr.requireNoNulls()
    }

    fun writeIdentityArray(arr: Array<Identifier>): ByteBuf {
        val size = arr.size
        arr.forEach { identity ->
            writeIdentity(identity)
        }
        return this
    }

    fun readIdentity(): Identifier = Identifier.of(readString())

    fun writeIdentity(identity: Identifier): ByteBuf {
        writeString(identity.toString())
        return this
    }

    fun readNBT(): NBTCompound? {
        val i = readerIndex()
        val b = readByte()
        return if (b.toInt() == 0) {
            null
        } else {
            readerIndex(i)
            val stream = ByteBufInputStream(this)
            stream.readNbt()
        }
    }

    fun writeNBT(tag: NBTCompound?) {
        if (tag == null)
            writeByte(0)
        else {
            val out = ByteBufOutputStream(this)
            out.write(tag)
        }
    }

    fun writeVarLong(i: Long): ByteBuf {
        var l = i
        while (l and -128L != 0L) {
            writeByte((l and 127L).toInt() or 128)
            l = l ushr 7
        }

        writeByte(l.toInt())
        return this
    }

    fun readPosition(): Position = Position.unpack(readLong())

    fun writePosition(pos: Position): ByteBuf = writeLong(pos.packed)

    companion object {
        @JvmStatic
        fun getVarIntSize(i: Int): Int {
            for (j in 1..4) {
                if (i and -1 shl j * 7 == 0) {
                    return j
                }
            }
            return 5
        }

        @JvmStatic
        inline fun readVarInt(next: () -> Byte): Int {
            var i = 0
            var j = 0
            var b: Int
            do {
                b = next().toInt()
                i = i or ((b and 127) shl j++ * 7)
                if (j > 5) {
                    throw RuntimeException("VarInt too big")
                }
            } while ((b and 128) == 128)
            return i
        }
    }
}