package org.sandboxpowered.silica.vanilla.network.play.clientbound.world

import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.util.BitPackedLongArray
import org.sandboxpowered.silica.world.state.block.BlockState
import org.sandboxpowered.silica.world.util.BlocTree
import org.sandboxpowered.silica.world.util.iterateCube

class VanillaChunkSection(
    blocTree: BlocTree,
    private val x: Int,
    private val y: Int,
    private val z: Int,
    private val stateToId: (BlockState) -> Int
) {

    private var nonAir: Int
    private val bitPacked: BitPackedLongArray

    init {
        val blocTreeSubSection = blocTree[x, y, z, 16, 16, 16]
        nonAir = blocTreeSubSection.nonAirInChunk(x, y, z)
        bitPacked = BitPackedLongArray(4096, BITS).apply {
            iterateCube(0, 0, 0, 16) { dx, dy, dz ->
                val state = blocTreeSubSection[x + dx, y + dy, z + dz]
                this[index(dx, dy, dz)] = stateToId(state)
            }
        }
    }

    fun write(to: PacketByteBuf) {
        to.writeShort(nonAir)
        to.writeByte(BITS) // TODO: optimize with palette
        // write nothing for palette as we are using global palette for now
        to.writeLongArray(bitPacked.data)
    }

    operator fun set(absolutePosition: Position, oldState: BlockState, newState: BlockState) {
        val dx = absolutePosition.x - x
        val dy = absolutePosition.y - y
        val dz = absolutePosition.z - z
        val idx = index(dx, dy, dz)
        bitPacked[idx] = stateToId(newState)
        if (oldState.isAir && !newState.isAir) --nonAir
        else if (!oldState.isAir && newState.isAir) ++nonAir
    }

    val serializedSize: Int get() = 3 + PacketByteBuf.getVarIntSize(bitPacked.data.size) + bitPacked.data.size * 8

    private fun index(x: Int, y: Int, z: Int): Int {
        return y shl 8 or (z shl 4) or x
    }

    private companion object {
        const val BITS = 15
    }
}