package org.sandboxpowered.silica.vanilla.network.play.clientbound

import io.netty.buffer.ByteBuf
import io.netty.buffer.Unpooled
import org.sandboxpowered.silica.nbt.CompoundTag
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.play.clientbound.world.VanillaChunkSection
import org.sandboxpowered.silica.vanilla.network.util.BitPackedLongArray
import org.sandboxpowered.silica.world.ChunkSectionPos
import org.sandboxpowered.silica.world.VanillaWorldAdapter
import org.sandboxpowered.silica.world.util.BlocTree
import java.util.*

class ChunkData() : PacketPlay {
    private val sections = arrayOfNulls<VanillaChunkSection>(16)
    private var cX = 0
    private var cZ = 0
    private var bitMask: LongArray = longArrayOf()
    private var buffer: ByteArray = byteArrayOf()
    private var biomes: IntArray = intArrayOf()

    constructor(cX: Int, cZ: Int, world: BlocTree, adapter: VanillaWorldAdapter) : this() {
        this.cX = cX
        this.cZ = cZ
        biomes = IntArray(1024)
        for (i in 0..15) {
            sections[i] = adapter.getVanillaChunkSection(ChunkSectionPos(cX, i, cZ))
        }
        buffer = ByteArray(calculateSize(cX, cZ))
        bitMask = extractData(PacketByteBuf(writeBuffer), cX, cZ, world).toLongArray()
    }

    private fun extractData(packetByteBuf: PacketByteBuf, cX: Int, cZ: Int, blocTree: BlocTree): BitSet {
        val mask = BitSet()
        for (i in 0..15) {
            sections[i]!!.write(packetByteBuf)
            mask.set(i) // TODO: only write non-empty
        }
        return mask
    }

    val readBuffer: PacketByteBuf
        get() = PacketByteBuf(Unpooled.wrappedBuffer(buffer))
    private val writeBuffer: ByteBuf
        private get() {
            val byteBuf = Unpooled.wrappedBuffer(buffer)
            byteBuf.writerIndex(0)
            return byteBuf
        }

    private fun calculateSize(cX: Int, cZ: Int): Int {
        var r = 0
        for (i in 0..15) {
            r += sections[i]!!.serializedSize
        }
        return r
    }

    override fun read(buf: PacketByteBuf) {}
    override fun write(buf: PacketByteBuf) {
        buf.writeInt(cX)
        buf.writeInt(cZ)
        buf.writeLongArray(bitMask)
        val heightmap = CompoundTag()
        val heightmapData = BitPackedLongArray(256, 9)
        for (i in 0..255) heightmapData[i] = 7
        heightmap.setLongArray("MOTION_BLOCKING", heightmapData.data)
        //        heightmap.setLongArray("WORLD_SURFACE", heightmapData.getData());
        buf.writeNBT(heightmap)
        buf.writeVarIntArray(biomes)
        buf.writeVarInt(buffer.size)
        buf.writeBytes(buffer)
        buf.writeVarInt(0)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}