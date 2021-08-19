package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.registry.Registry
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.state.SilicaStateFactory
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState
import org.sandboxpowered.silica.util.Identifier

class BaseBlock(override val identifier: Identifier) : Block {
    override fun isAir(state: BlockState): Boolean {
        return identifier.path == "air"
    }

    override val stateProvider: StateProvider<Block, BlockState> = SilicaStateFactory(this, emptyMap(), BlockState.factory)
    override val defaultState: BlockState
        get() = stateProvider.baseState
    override val registry: Registry<Block>
        get() = SilicaRegistries.BLOCK_REGISTRY
}