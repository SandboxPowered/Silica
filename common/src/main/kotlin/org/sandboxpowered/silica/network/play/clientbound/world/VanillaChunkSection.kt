package org.sandboxpowered.silica.network.play.clientbound.world

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.util.BitPackedLongArray
import org.sandboxpowered.silica.world.util.BlocTree

class VanillaChunkSection(
    blocTree: BlocTree, private val x: Int, private val y: Int, private val z: Int
) {

    private val blocTree = blocTree[x, y, z, 16, 16, 16]
    private val bitPacked = BitPackedLongArray(4096, BITS).apply {
        repeat(16) { x ->
            repeat(8) { y ->
                repeat(16) { z ->
                    if (x % 2 == 0) this[index(x, y, z)] = when (y) {
                        0, 1, 2 -> STONE
                        3 -> COBBLESTONE
                        4, 5 -> DIRT
                        6 -> GRASS_NO_SNOW
                        else -> AIR
                    }
                }
            }
        }
    }

    fun t(map: Map<String, Int>) {
        for (e in map.entries) {
            val k = e.key
            val v = e.value
            println("$k -> $v")
        }
        for ((k, v) in map.entries) {
            println("$k -> $v")
        }
        map.forEach { (k, v) ->
            println("$k -> $v")
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
        const val BITS = 14

        const val AIR = 0
        const val STONE = 1
        const val GRASS_NO_SNOW = 9
        const val DIRT = 10
        const val COBBLESTONE = 14
    }
}