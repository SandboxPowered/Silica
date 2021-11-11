package org.sandboxpowered.silica.api.block

import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.world.state.SilicaStateBuilder
import org.sandboxpowered.silica.api.world.state.SilicaStateFactory
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState

open class BaseBlock(final override val identifier: Identifier) : Block {
    override val isAir: Boolean
        get() = identifier.path == "air"

    override val item: Item? by Registries.ITEMS[identifier].optional

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
        get() = Registries.BLOCKS
}