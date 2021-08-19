package org.sandboxpowered.silica.network.play.clientbound.world

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.util.BitPackedLongArray
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.world.util.BlocTree
import org.sandboxpowered.silica.world.util.iterateCube

class VanillaChunkSection(
    blocTree: BlocTree,
    private val x: Int,
    private val y: Int,
    private val z: Int,
    private val stateToId: (BlockState) -> Int
) {

    private val blocTree = blocTree[x, y, z, 16, 16, 16]
    private val bitPacked = BitPackedLongArray(4096, BITS).apply {
        iterateCube(0, 0, 0, 16) { dx, dy, dz ->
            val state = this@VanillaChunkSection.blocTree[x + dx, y + dy, z + dz]
            this[index(dx, dy, dz)] = stateToId(state)
        }
    }

    fun write(to: PacketByteBuf) {
        to.writeShort(blocTree.nonAirInChunk(x, y, z))
        to.writeByte(BITS) // TODO: optimize with palette
        // write nothing for palette as we are using global palette for now
        to.writeLongArray(bitPacked.data)
    }

    val serializedSize: Int get() = 3 + PacketByteBuf.getVarIntSize(bitPacked.data.size) + bitPacked.data.size * 8

    private fun index(x: Int, y: Int, z: Int): Int {
        return y shl 8 or (z shl 4) or x
    }

    private companion object {
        const val BITS = 15
    }
}