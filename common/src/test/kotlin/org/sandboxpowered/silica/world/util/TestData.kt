package org.sandboxpowered.silica.world.util

import com.google.common.collect.ImmutableMap
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.sandboxpowered.silica.block.Block
import org.sandboxpowered.silica.registry.Registry
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.state.block.BlockState

object TestData {

    init {
        mockkStatic(Registry::class)
        val blocks = mockk<Registry<Block>>(relaxed = true)
        every { SilicaRegistries.BLOCK_REGISTRY } returns blocks
    }

    fun block(name: String, isAir: Boolean = false): Block = mockk(name, relaxed = true) {
        every { isAir(any()) } returns isAir
    }

    fun state(blockName: String, isAir: Boolean = false) =
        BlockState(
            block(blockName, isAir),
            ImmutableMap.of()
        )
}