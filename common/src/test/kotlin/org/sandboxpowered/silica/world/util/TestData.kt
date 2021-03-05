package org.sandboxpowered.silica.world.util

import com.google.common.collect.ImmutableMap
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.sandboxpowered.api.block.Block
import org.sandboxpowered.api.registry.Registry
import org.sandboxpowered.silica.state.block.SilicaBlockState

object TestData {

    init {
        mockkStatic(Registry::class)
        val blocks = mockk<Registry<Block>>(relaxed = true)
        every { Registry.getRegistryFromType(Block::class.java) } returns blocks
    }

    fun block(name: String, isAir: Boolean = false): Block = mockk(name, relaxed = true) {
        every { isAir(any()) } returns isAir
    }

    fun state(blockName: String, isAir: Boolean = false) =
        SilicaBlockState(
            block(blockName, isAir),
            ImmutableMap.of()
        )
}