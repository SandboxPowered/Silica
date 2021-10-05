package org.sandboxpowered.silica.content.block

import net.kyori.adventure.translation.Translatable
import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

sealed interface Block : RegistryEntry<Block>, Translatable {
    val item: Item?
    val isAir: Boolean

    override fun translationKey(): String = "block.${identifier.namespace}.${identifier.path}"

    val defaultState: BlockState
    val stateProvider: StateProvider<Block, BlockState>
}