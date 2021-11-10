package org.sandboxpowered.silica.api.block

import net.kyori.adventure.translation.Translatable
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.api.world.WorldWriter
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState

sealed interface Block : RegistryEntry<Block>, Translatable {
    val item: Item?
    val isAir: Boolean

    val hasItem: Boolean get() = !isAir

    override fun translationKey(): String = "block.${identifier.namespace}.${identifier.path}"

    /**
     * Called when block next to this one changes state with the flag [WorldWriter.Flag.NOTIFY_NEIGHBORS]
     */
    fun onNeighborUpdate(
        world: World,
        pos: Position,
        state: BlockState,
        origin: Position,
        originState: BlockState,
        side: Direction
    ) = Unit

    val defaultState: BlockState
    val stateProvider: StateProvider<Block, BlockState>
}