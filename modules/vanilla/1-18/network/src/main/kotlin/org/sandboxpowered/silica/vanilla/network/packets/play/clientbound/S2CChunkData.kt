package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.sandboxpowered.silica.api.nbt.CompoundTag
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.packets.play.clientbound.world.VanillaChunkSection
import org.sandboxpowered.silica.vanilla.network.util.BitPackedLongArray
import org.sandboxpowered.silica.vanilla.network.util.PacketByteBuf
import java.util.*

class S2CChunkData(
    private val cX: Int,
    private val cZ: Int,
    private val sections: Array<out VanillaChunkSection>
) : PacketPlay {
    private var bitMask: LongArray = longArrayOf()
    private var buffer: ByteArray = byteArrayOf()
    private var biomes: IntArray = intArrayOf()

    constructor(buf: PacketBuffer) : this(
        buf.readInt(), buf.readInt(), TODO("Not implemented")
    )

    init {
        biomes = IntArray(1024)
        buffer = ByteArray(calculateSize(cX, cZ))
        bitMask = extractData(PacketByteBuf(writeBuffer), cX, cZ).toLongArray()
    }

    private fun extractData(packetByteBuf: PacketByteBuf, cX: Int, cZ: Int): BitSet {
        val mask = BitSet()
        for (i in 0..15) {
            sections[i].write(packetByteBuf)
            mask.set(i) // TODO: only write non-empty
        }
        return mask
    }

    val readBuffer: PacketByteBuf
        get() = PacketByteBuf(Unpooled.wrappedBuffer(buffer))
    private val writeBuffer: ByteBuf
        get() {
            val byteBuf = Unpooled.wrappedBuffer(buffer)
            byteBuf.writerIndex(0)
            return byteBuf
        }

    private fun calculateSize(cX: Int, cZ: Int): Int {
        var r = 0
        for (i in 0..15) {
            r += sections[i].serializedSize
        }
        return r
    }

    override fun write(buf: PacketBuffer) {
        buf.writeInt(cX)
        buf.writeInt(cZ)
//        buf.writeLongArray(bitMask)
        // ChunkData
        val heightmap = CompoundTag()
        val heightmapData = BitPackedLongArray(256, 10)
        for (i in 0..255) heightmapData[i] = 8
        heightmap.setLongArray("MOTION_BLOCKING", heightmapData.data)
        //        heightmap.setLongArray("WORLD_SURFACE", heightmapData.getData());
        buf.writeNBT(heightmap)
//        buf.writeVarIntArray(biomes)
        buf.writeVarInt(buffer.size)
        buf.writeBytes(buffer)
        buf.writeLongArray(longArrayOf())
        // LightData
        buf.writeBoolean(true)
        buf.writeLongArray(BitSet(18).toLongArray())
        buf.writeLongArray(BitSet(18).toLongArray())
        buf.writeLongArray(BitSet(18).toLongArray())
        buf.writeLongArray(BitSet(18).toLongArray())
        buf.writeVarIntArray(intArrayOf())
        buf.writeVarIntArray(intArrayOf())
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}