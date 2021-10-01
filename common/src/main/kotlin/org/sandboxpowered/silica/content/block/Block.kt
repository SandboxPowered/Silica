package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

sealed interface Block : RegistryEntry<Block> {
    val item: Item?
    val isAir: Boolean

    val defaultState: BlockState
    val stateProvider: StateProvider<Block, BlockState>
}