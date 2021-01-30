package org.sandboxpowered.silica.network.util

import kotlin.math.ceil

class BitPackedLongArray(
    /**
     * Amount of elements in the array
     */
    val size: Int,

    /**
     * Amount of bits to use per element
     */
    val bits: Int
) {
    init {
        require(bits in 0..32) { "Bits should be in range [0,32]; provided: $bits" }
    }

    private val valuesPerLong = 64 / bits
    private val mask = 0xFFFFFFFF ushr (32 - bits)

    /**
     * Access to the real array is given to prevent copying.
     * Please do not edit this manually
     */
    val data = LongArray(ceil(size.toDouble() / valuesPerLong).toInt())

    operator fun get(idx: Int): Int {
        require(idx in 0 until size) { "Idx should be in range [0,$size[; provided: $idx" }
        val l = data[realIndex(idx)]
        val slot = slot(idx)
        return (l shr (slot * bits) and mask).toInt()
    }

    operator fun set(idx: Int, value: Int) {
        require(idx in 0 until size) { "Idx should be in range [0,$size[; provided: $idx" }
        val realIndex = realIndex(idx)
        val slot = slot(idx)
        val shifts = slot * bits
        val v = value.toLong() and mask
        val l = data[realIndex] and (mask shl shifts).inv()
        data[realIndex] = l or (v shl shifts)
    }

    private fun realIndex(idx: Int): Int = idx / valuesPerLong

    private fun slot(idx: Int): Int = idx % valuesPerLong
}
