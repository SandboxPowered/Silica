package org.sandboxpowered.silica.api.block

import net.kyori.adventure.translation.Translatable
import org.joml.Vector3f
import org.sandboxpowered.silica.api.entity.EntityContext
import org.sandboxpowered.silica.api.entity.InteractionContext
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.util.ActionResult
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.Hand
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.api.world.WorldReader
import org.sandboxpowered.silica.api.world.WorldWriter
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState

interface Block : RegistryEntry<Block>, Translatable {
    companion object {
        val AIR by Registries.BLOCKS
    }

    val item: Item?
    val isAir: Boolean
    fun isAir(state: BlockState): Boolean = isAir

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

    fun onUse(
        world: World,
        pos: Position,
        state: BlockState,
        hand: Hand,
        face: Direction,
        cursor: Vector3f,
        ctx: EntityContext
    ): ActionResult = ActionResult.PASS

    fun getStateForPlacement(
        world: WorldReader,
        pos: Position,
        interaction: InteractionContext,
        ctx: EntityContext
    ): BlockState = defaultState

    val defaultState: BlockState
    val stateProvider: StateProvider<Block, BlockState>

    override val registry: Registry<Block> get() = Registries.BLOCKS
}