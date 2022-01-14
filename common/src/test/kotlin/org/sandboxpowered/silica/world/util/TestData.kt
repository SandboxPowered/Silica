package org.sandboxpowered.silica.world.util

import com.google.common.collect.ImmutableMap
import io.mockk.every
import io.mockk.mockk
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.utilities.Identifier
import org.sandboxpowered.silica.api.world.state.block.BlockState

object TestData {
    fun block(name: String, air: Boolean = false): Block = mockk(name, relaxed = true) {
        every { identifier } returns Identifier(name)
        every { isAir } returns air
    }

    fun state(blockName: String, isAir: Boolean = false) = BlockState(block(blockName, isAir), ImmutableMap.of())
}