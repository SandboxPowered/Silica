package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.registry.Registry
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.state.SilicaStateBuilder
import org.sandboxpowered.silica.state.SilicaStateFactory
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

open class BaseBlock(override val identifier: Identifier) : Block {
    override fun isAir(state: BlockState): Boolean {
        return identifier.path == "air"
    }

    protected open fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) = Unit

    protected open fun createDefaultState(baseState: BlockState): BlockState = baseState

    override val stateProvider: StateProvider<Block, BlockState> by lazy {
        SilicaStateFactory(
            this,
            SilicaStateBuilder<Block, BlockState>(this).apply { appendProperties(this) }.getProperties(),
            BlockState.factory
        )
    }
    override val defaultState: BlockState by lazy { createDefaultState(stateProvider.baseState) }
    override val registry: Registry<Block>
        get() = SilicaRegistries.BLOCK_REGISTRY
}