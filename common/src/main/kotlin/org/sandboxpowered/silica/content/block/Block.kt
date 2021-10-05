package org.sandboxpowered.silica.content.block

import net.kyori.adventure.translation.Translatable
import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.util.content.Direction
import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.world.World
import org.sandboxpowered.silica.world.WorldWriter
import org.sandboxpowered.silica.world.state.StateProvider
import org.sandboxpowered.silica.world.state.block.BlockState

sealed interface Block : RegistryEntry<Block>, Translatable {
    val item: Item?
    val isAir: Boolean

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