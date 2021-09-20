package org.sandboxpowered.silica.world.util

import com.google.common.collect.ImmutableMap
import io.mockk.every
import io.mockk.mockk
import org.sandboxpowered.silica.block.Block
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

object TestData {
    fun block(name: String, isAir: Boolean = false): Block = mockk(name, relaxed = true) {
        every { identifier } returns Identifier.of(name)
        every { isAir(any()) } returns isAir
    }

    fun state(blockName: String, isAir: Boolean = false) = BlockState(block(blockName, isAir), ImmutableMap.of())
}