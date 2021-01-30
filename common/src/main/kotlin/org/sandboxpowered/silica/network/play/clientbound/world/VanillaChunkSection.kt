package org.sandboxpowered.silica.network.play.clientbound.world

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.util.BitPackedLongArray
import org.sandboxpowered.silica.world.util.BlocTree

class VanillaChunkSection(
    blocTree: BlocTree, private val x: Int, private val y: Int, private val z: Int
) {

    private val blocTree = blocTree[x, y, z, 16, 16, 16]

    fun write(to: PacketByteBuf) {
        to.writeShort(blocTree.nonAirInChunk(x, y, z))
        to.writeByte(BITS) // TODO: optimize with palette
        // write nothing for palette as we are using global palette for now
        to.writeLongArray(BitPackedLongArray(4096, BITS).apply {
            repeat(16) { x ->
                repeat(8) { y ->
                    repeat(16) { z ->
                        if (x % 2 == 0) this[index(x, y, z)] = when (y) {
                            0, 1, 2 -> STONE
                            3 -> COBBLESTONE
                            4, 5 -> DIRT
                            6, 7 -> GRASS_NO_SNOW
                            else -> AIR
                        }
                    }
                }
            }
        }.data)
    }

    private fun index(x: Int, y: Int, z: Int): Int {
        return y shl 8 or (z shl 4) or x
    }

    private companion object {
        const val BITS = 14

        const val AIR = 0
        const val STONE = 1
        const val GRASS_NO_SNOW = 9
        const val DIRT = 10
        const val COBBLESTONE = 14
    }
}