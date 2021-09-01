package org.sandboxpowered.silica.network

import io.netty.buffer.ByteBuf
import io.netty.buffer.ByteBufAllocator
import io.netty.buffer.ByteBufInputStream
import io.netty.buffer.ByteBufOutputStream
import io.netty.handler.codec.DecoderException
import io.netty.handler.codec.EncoderException
import io.netty.util.ByteProcessor
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
    override fun capacity(): Int {
        return source.capacity()
    }

    override fun capacity(i: Int): ByteBuf {
        return source.capacity(i)
    }

    override fun maxCapacity(): Int {
        return source.maxCapacity()
    }

    override fun alloc(): ByteBufAllocator {
        return source.alloc()
    }

    override fun order(): ByteOrder {
        return source.order()
    }

    override fun order(byteOrder: ByteOrder): ByteBuf {
        return source.order(byteOrder)
    }

    override fun unwrap(): ByteBuf {
        return source.unwrap()
    }

    override fun isDirect(): Boolean {
        return source.isDirect
    }

    override fun isReadOnly(): Boolean {
        return source.isReadOnly
    }

    override fun asReadOnly(): ByteBuf {
        return source.asReadOnly()
    }

    override fun readerIndex(): Int {
        return source.readerIndex()
    }

    override fun readerIndex(i: Int): ByteBuf {
        return source.readerIndex(i)
    }

    override fun writerIndex(): Int {
        return source.writerIndex()
    }

    override fun writerIndex(i: Int): ByteBuf {
        return source.writerIndex(i)
    }

    override fun setIndex(i: Int, j: Int): ByteBuf {
        return source.setIndex(i, j)
    }

    override fun readableBytes(): Int {
        return source.readableBytes()
    }

    override fun writableBytes(): Int {
        return source.writableBytes()
    }

    override fun maxWritableBytes(): Int {
        return source.maxWritableBytes()
    }

    override fun isReadable(): Boolean {
        return source.isReadable
    }

    override fun isReadable(i: Int): Boolean {
        return source.isReadable(i)
    }

    override fun isWritable(): Boolean {
        return source.isWritable
    }

    override fun isWritable(i: Int): Boolean {
        return source.isWritable(i)
    }

    override fun clear(): ByteBuf {
        return source.clear()
    }

    override fun markReaderIndex(): ByteBuf {
        return source.markReaderIndex()
    }

    override fun resetReaderIndex(): ByteBuf {
        return source.resetReaderIndex()
    }

    override fun markWriterIndex(): ByteBuf {
        return source.markWriterIndex()
    }

    override fun resetWriterIndex(): ByteBuf {
        return source.resetWriterIndex()
    }

    override fun discardReadBytes(): ByteBuf {
        return source.discardReadBytes()
    }

    override fun discardSomeReadBytes(): ByteBuf {
        return source.discardSomeReadBytes()
    }

    override fun ensureWritable(i: Int): ByteBuf {
        return source.ensureWritable(i)
    }

    override fun ensureWritable(i: Int, bl: Boolean): Int {
        return source.ensureWritable(i, bl)
    }

    override fun getBoolean(i: Int): Boolean {
        return source.getBoolean(i)
    }

    override fun getByte(i: Int): Byte {
        return source.getByte(i)
    }

    override fun getUnsignedByte(i: Int): Short {
        return source.getUnsignedByte(i)
    }

    override fun getShort(i: Int): Short {
        return source.getShort(i)
    }

    override fun getShortLE(i: Int): Short {
        return source.getShortLE(i)
    }

    override fun getUnsignedShort(i: Int): Int {
        return source.getUnsignedShort(i)
    }

    override fun getUnsignedShortLE(i: Int): Int {
        return source.getUnsignedShortLE(i)
    }

    override fun getMedium(i: Int): Int {
        return source.getMedium(i)
    }

    override fun getMediumLE(i: Int): Int {
        return source.getMediumLE(i)
    }

    override fun getUnsignedMedium(i: Int): Int {
        return source.getUnsignedMedium(i)
    }

    override fun getUnsignedMediumLE(i: Int): Int {
        return source.getUnsignedMediumLE(i)
    }

    override fun getInt(i: Int): Int {
        return source.getInt(i)
    }

    override fun getIntLE(i: Int): Int {
        return source.getIntLE(i)
    }

    override fun getUnsignedInt(i: Int): Long {
        return source.getUnsignedInt(i)
    }

    override fun getUnsignedIntLE(i: Int): Long {
        return source.getUnsignedIntLE(i)
    }

    override fun getLong(i: Int): Long {
        return source.getLong(i)
    }

    override fun getLongLE(i: Int): Long {
        return source.getLongLE(i)
    }

    override fun getChar(i: Int): Char {
        return source.getChar(i)
    }

    override fun getFloat(i: Int): Float {
        return source.getFloat(i)
    }

    override fun getDouble(i: Int): Double {
        return source.getDouble(i)
    }

    override fun getBytes(i: Int, byteBuf: ByteBuf): ByteBuf {
        return source.getBytes(i, byteBuf)
    }

    override fun getBytes(i: Int, byteBuf: ByteBuf, j: Int): ByteBuf {
        return source.getBytes(i, byteBuf, j)
    }

    override fun getBytes(i: Int, byteBuf: ByteBuf, j: Int, k: Int): ByteBuf {
        return source.getBytes(i, byteBuf, j, k)
    }

    override fun getBytes(i: Int, bs: ByteArray): ByteBuf {
        return source.getBytes(i, bs)
    }

    override fun getBytes(i: Int, bs: ByteArray, j: Int, k: Int): ByteBuf {
        return source.getBytes(i, bs, j, k)
    }

    override fun getBytes(i: Int, byteBuffer: ByteBuffer): ByteBuf {
        return source.getBytes(i, byteBuffer)
    }

    @Throws(IOException::class)
    override fun getBytes(i: Int, outputStream: OutputStream, j: Int): ByteBuf {
        return source.getBytes(i, outputStream, j)
    }

    @Throws(IOException::class)
    override fun getBytes(i: Int, gatheringByteChannel: GatheringByteChannel, j: Int): Int {
        return source.getBytes(i, gatheringByteChannel, j)
    }

    @Throws(IOException::class)
    override fun getBytes(i: Int, fileChannel: FileChannel, l: Long, j: Int): Int {
        return source.getBytes(i, fileChannel, l, j)
    }

    override fun getCharSequence(i: Int, j: Int, charset: Charset): CharSequence {
        return source.getCharSequence(i, j, charset)
    }

    override fun setBoolean(i: Int, bl: Boolean): ByteBuf {
        return source.setBoolean(i, bl)
    }

    override fun setByte(i: Int, j: Int): ByteBuf {
        return source.setByte(i, j)
    }

    override fun setShort(i: Int, j: Int): ByteBuf {
        return source.setShort(i, j)
    }

    override fun setShortLE(i: Int, j: Int): ByteBuf {
        return source.setShortLE(i, j)
    }

    override fun setMedium(i: Int, j: Int): ByteBuf {
        return source.setMedium(i, j)
    }

    override fun setMediumLE(i: Int, j: Int): ByteBuf {
        return source.setMediumLE(i, j)
    }

    override fun setInt(i: Int, j: Int): ByteBuf {
        return source.setInt(i, j)
    }

    override fun setIntLE(i: Int, j: Int): ByteBuf {
        return source.setIntLE(i, j)
    }

    override fun setLong(i: Int, l: Long): ByteBuf {
        return source.setLong(i, l)
    }

    override fun setLongLE(i: Int, l: Long): ByteBuf {
        return source.setLongLE(i, l)
    }

    override fun setChar(i: Int, j: Int): ByteBuf {
        return source.setChar(i, j)
    }

    override fun setFloat(i: Int, f: Float): ByteBuf {
        return source.setFloat(i, f)
    }

    override fun setDouble(i: Int, d: Double): ByteBuf {
        return source.setDouble(i, d)
    }

    override fun setBytes(i: Int, byteBuf: ByteBuf): ByteBuf {
        return source.setBytes(i, byteBuf)
    }

    override fun setBytes(i: Int, byteBuf: ByteBuf, j: Int): ByteBuf {
        return source.setBytes(i, byteBuf, j)
    }

    override fun setBytes(i: Int, byteBuf: ByteBuf, j: Int, k: Int): ByteBuf {
        return source.setBytes(i, byteBuf, j, k)
    }

    override fun setBytes(i: Int, bs: ByteArray): ByteBuf {
        return source.setBytes(i, bs)
    }

    override fun setBytes(i: Int, bs: ByteArray, j: Int, k: Int): ByteBuf {
        return source.setBytes(i, bs, j, k)
    }

    override fun setBytes(i: Int, byteBuffer: ByteBuffer): ByteBuf {
        return source.setBytes(i, byteBuffer)
    }

    @Throws(IOException::class)
    override fun setBytes(i: Int, inputStream: InputStream, j: Int): Int {
        return source.setBytes(i, inputStream, j)
    }

    @Throws(IOException::class)
    override fun setBytes(i: Int, scatteringByteChannel: ScatteringByteChannel, j: Int): Int {
        return source.setBytes(i, scatteringByteChannel, j)
    }

    @Throws(IOException::class)
    override fun setBytes(i: Int, fileChannel: FileChannel, l: Long, j: Int): Int {
        return source.setBytes(i, fileChannel, l, j)
    }

    override fun setZero(i: Int, j: Int): ByteBuf {
        return source.setZero(i, j)
    }

    override fun setCharSequence(i: Int, charSequence: CharSequence, charset: Charset): Int {
        return source.setCharSequence(i, charSequence, charset)
    }

    override fun readBoolean(): Boolean {
        return source.readBoolean()
    }

    override fun readByte(): Byte {
        return source.readByte()
    }

    override fun readUnsignedByte(): Short {
        return source.readUnsignedByte()
    }

    override fun readShort(): Short {
        return source.readShort()
    }

    override fun readShortLE(): Short {
        return source.readShortLE()
    }

    override fun readUnsignedShort(): Int {
        return source.readUnsignedShort()
    }

    override fun readUnsignedShortLE(): Int {
        return source.readUnsignedShortLE()
    }

    override fun readMedium(): Int {
        return source.readMedium()
    }

    override fun readMediumLE(): Int {
        return source.readMediumLE()
    }

    override fun readUnsignedMedium(): Int {
        return source.readUnsignedMedium()
    }

    override fun readUnsignedMediumLE(): Int {
        return source.readUnsignedMediumLE()
    }

    override fun readInt(): Int {
        return source.readInt()
    }

    override fun readIntLE(): Int {
        return source.readIntLE()
    }

    override fun readUnsignedInt(): Long {
        return source.readUnsignedInt()
    }

    override fun readUnsignedIntLE(): Long {
        return source.readUnsignedIntLE()
    }

    override fun readLong(): Long {
        return source.readLong()
    }

    override fun readLongLE(): Long {
        return source.readLongLE()
    }

    override fun readChar(): Char {
        return source.readChar()
    }

    override fun readFloat(): Float {
        return source.readFloat()
    }

    override fun readDouble(): Double {
        return source.readDouble()
    }

    override fun readBytes(i: Int): ByteBuf {
        return source.readBytes(i)
    }

    override fun readSlice(i: Int): ByteBuf {
        return source.readSlice(i)
    }

    override fun readRetainedSlice(i: Int): ByteBuf {
        return source.readRetainedSlice(i)
    }

    override fun readBytes(byteBuf: ByteBuf): ByteBuf {
        return source.readBytes(byteBuf)
    }

    override fun readBytes(byteBuf: ByteBuf, i: Int): ByteBuf {
        return source.readBytes(byteBuf, i)
    }

    override fun readBytes(byteBuf: ByteBuf, i: Int, j: Int): ByteBuf {
        return source.readBytes(byteBuf, i, j)
    }

    override fun readBytes(bs: ByteArray): ByteBuf {
        return source.readBytes(bs)
    }

    override fun readBytes(bs: ByteArray, i: Int, j: Int): ByteBuf {
        return source.readBytes(bs, i, j)
    }

    override fun readBytes(byteBuffer: ByteBuffer): ByteBuf {
        return source.readBytes(byteBuffer)
    }

    @Throws(IOException::class)
    override fun readBytes(outputStream: OutputStream, i: Int): ByteBuf {
        return source.readBytes(outputStream, i)
    }

    @Throws(IOException::class)
    override fun readBytes(gatheringByteChannel: GatheringByteChannel, i: Int): Int {
        return source.readBytes(gatheringByteChannel, i)
    }

    override fun readCharSequence(i: Int, charset: Charset): CharSequence {
        return source.readCharSequence(i, charset)
    }

    @Throws(IOException::class)
    override fun readBytes(fileChannel: FileChannel, l: Long, i: Int): Int {
        return source.readBytes(fileChannel, l, i)
    }

    override fun skipBytes(i: Int): ByteBuf {
        return source.skipBytes(i)
    }

    override fun writeBoolean(bl: Boolean): ByteBuf {
        return source.writeBoolean(bl)
    }

    override fun writeByte(i: Int): ByteBuf {
        return source.writeByte(i)
    }

    override fun writeShort(i: Int): ByteBuf {
        return source.writeShort(i)
    }

    override fun writeShortLE(i: Int): ByteBuf {
        return source.writeShortLE(i)
    }

    override fun writeMedium(i: Int): ByteBuf {
        return source.writeMedium(i)
    }

    override fun writeMediumLE(i: Int): ByteBuf {
        return source.writeMediumLE(i)
    }

    override fun writeInt(i: Int): ByteBuf {
        return source.writeInt(i)
    }

    override fun writeIntLE(i: Int): ByteBuf {
        return source.writeIntLE(i)
    }

    override fun writeLong(l: Long): ByteBuf {
        return source.writeLong(l)
    }

    override fun writeLongLE(l: Long): ByteBuf {
        return source.writeLongLE(l)
    }

    override fun writeChar(i: Int): ByteBuf {
        return source.writeChar(i)
    }

    override fun writeFloat(f: Float): ByteBuf {
        return source.writeFloat(f)
    }

    override fun writeDouble(d: Double): ByteBuf {
        return source.writeDouble(d)
    }

    override fun writeBytes(byteBuf: ByteBuf): ByteBuf {
        return source.writeBytes(byteBuf)
    }

    override fun writeBytes(byteBuf: ByteBuf, i: Int): ByteBuf {
        return source.writeBytes(byteBuf, i)
    }

    override fun writeBytes(byteBuf: ByteBuf, i: Int, j: Int): ByteBuf {
        return source.writeBytes(byteBuf, i, j)
    }

    override fun writeBytes(bs: ByteArray): ByteBuf {
        return source.writeBytes(bs)
    }

    override fun writeBytes(bs: ByteArray, i: Int, j: Int): ByteBuf {
        return source.writeBytes(bs, i, j)
    }

    override fun writeBytes(byteBuffer: ByteBuffer): ByteBuf {
        return source.writeBytes(byteBuffer)
    }

    @Throws(IOException::class)
    override fun writeBytes(inputStream: InputStream, i: Int): Int {
        return source.writeBytes(inputStream, i)
    }

    @Throws(IOException::class)
    override fun writeBytes(scatteringByteChannel: ScatteringByteChannel, i: Int): Int {
        return source.writeBytes(scatteringByteChannel, i)
    }

    @Throws(IOException::class)
    override fun writeBytes(fileChannel: FileChannel, l: Long, i: Int): Int {
        return source.writeBytes(fileChannel, l, i)
    }

    override fun writeZero(i: Int): ByteBuf {
        return source.writeZero(i)
    }

    override fun writeCharSequence(charSequence: CharSequence, charset: Charset): Int {
        return source.writeCharSequence(charSequence, charset)
    }

    override fun indexOf(i: Int, j: Int, b: Byte): Int {
        return source.indexOf(i, j, b)
    }

    override fun bytesBefore(b: Byte): Int {
        return source.bytesBefore(b)
    }

    override fun bytesBefore(i: Int, b: Byte): Int {
        return source.bytesBefore(i, b)
    }

    override fun bytesBefore(i: Int, j: Int, b: Byte): Int {
        return source.bytesBefore(i, j, b)
    }

    override fun forEachByte(byteProcessor: ByteProcessor): Int {
        return source.forEachByte(byteProcessor)
    }

    override fun forEachByte(i: Int, j: Int, byteProcessor: ByteProcessor): Int {
        return source.forEachByte(i, j, byteProcessor)
    }

    override fun forEachByteDesc(byteProcessor: ByteProcessor): Int {
        return source.forEachByteDesc(byteProcessor)
    }

    override fun forEachByteDesc(i: Int, j: Int, byteProcessor: ByteProcessor): Int {
        return source.forEachByteDesc(i, j, byteProcessor)
    }

    override fun copy(): ByteBuf {
        return source.copy()
    }

    override fun copy(i: Int, j: Int): ByteBuf {
        return source.copy(i, j)
    }

    override fun slice(): ByteBuf {
        return source.slice()
    }

    override fun retainedSlice(): ByteBuf {
        return source.retainedSlice()
    }

    override fun slice(i: Int, j: Int): ByteBuf {
        return source.slice(i, j)
    }

    override fun retainedSlice(i: Int, j: Int): ByteBuf {
        return source.retainedSlice(i, j)
    }

    override fun duplicate(): ByteBuf {
        return source.duplicate()
    }

    override fun retainedDuplicate(): ByteBuf {
        return source.retainedDuplicate()
    }

    override fun nioBufferCount(): Int {
        return source.nioBufferCount()
    }

    override fun nioBuffer(): ByteBuffer {
        return source.nioBuffer()
    }

    override fun nioBuffer(i: Int, j: Int): ByteBuffer {
        return source.nioBuffer(i, j)
    }

    override fun internalNioBuffer(i: Int, j: Int): ByteBuffer {
        return source.internalNioBuffer(i, j)
    }

    override fun nioBuffers(): Array<ByteBuffer> {
        return source.nioBuffers()
    }

    override fun nioBuffers(i: Int, j: Int): Array<ByteBuffer> {
        return source.nioBuffers(i, j)
    }

    override fun hasArray(): Boolean {
        return source.hasArray()
    }

    override fun array(): ByteArray {
        return source.array()
    }

    override fun arrayOffset(): Int {
        return source.arrayOffset()
    }

    override fun hasMemoryAddress(): Boolean {
        return source.hasMemoryAddress()
    }

    override fun memoryAddress(): Long {
        return source.memoryAddress()
    }

    override fun toString(charset: Charset): String {
        return source.toString(charset)
    }

    override fun toString(i: Int, j: Int, charset: Charset): String {
        return source.toString(i, j, charset)
    }

    override fun hashCode(): Int {
        return source.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        return source == other
    }

    override fun compareTo(other: ByteBuf): Int {
        return source.compareTo(other)
    }

    override fun toString(): String {
        return source.toString()
    }

    override fun retain(i: Int): ByteBuf {
        return source.retain(i)
    }

    override fun retain(): ByteBuf {
        return source.retain()
    }

    override fun touch(): ByteBuf {
        return source.touch()
    }

    override fun touch(`object`: Any): ByteBuf {
        return source.touch(`object`)
    }

    override fun refCnt(): Int {
        return source.refCnt()
    }

    override fun release(): Boolean {
        return source.release()
    }

    override fun release(i: Int): Boolean {
        return source.release(i)
    }

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

    fun readUUID(): UUID {
        return UUID(readLong(), readLong())
    }

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

    fun readIdentity(): Identifier {
        return Identifier.of(readString())
    }

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