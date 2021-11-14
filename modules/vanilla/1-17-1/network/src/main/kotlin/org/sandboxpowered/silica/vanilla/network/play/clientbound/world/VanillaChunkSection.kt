package org.sandboxpowered.silica.vanilla.network.play.clientbound.world

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.WorldReader
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.util.BitPackedLongArray

class VanillaChunkSection(
    world: WorldReader,
    private val x: Int,
    private val y: Int,
    private val z: Int,
    private val stateToId: (BlockState) -> Int
) {

    private var nonAir: Int
    private val bitPacked: BitPackedLongArray

    init {
        val blocTreeSubSection = world.subsection(x, y, z, 16, 16, 16) //[x, y, z, 16, 16, 16]
        nonAir = blocTreeSubSection.nonAirInChunk(x, y, z)
        bitPacked = BitPackedLongArray(4096, BITS).apply {
            iterateCube(0, 0, 0, 16) { dx, dy, dz ->
                val state = blocTreeSubSection.getBlockState(x + dx, y + dy, z + dz)
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

    val serializedSize: Int get() = 3 + PacketBuffer.getVarIntSize(bitPacked.data.size) + bitPacked.data.size * 8

    private fun index(x: Int, y: Int, z: Int): Int = y shl 8 or (z shl 4) or x

    private companion object {
        const val BITS = 15
    }
}

inline fun iterateCube(x: Int, y: Int, z: Int, w: Int, h: Int = w, d: Int = w, iter: (x: Int, y: Int, z: Int) -> Unit) {
    repeat(w) { dx ->
        repeat(h) { dy ->
            repeat(d) { dz ->
                iter(x + dx, y + dy, z + dz)
            }
        }
    }
}
