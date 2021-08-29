package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.state.StateProvider
import org.sandboxpowered.silica.state.block.BlockState

sealed interface Block : RegistryEntry<Block> {
    fun isAir(state: BlockState): Boolean

    val defaultState: BlockState
    val stateProvider: StateProvider<Block, BlockState>
}