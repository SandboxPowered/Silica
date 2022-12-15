package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.entity.EntityContext
import org.sandboxpowered.silica.api.entity.InteractionContext
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.WorldReader
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.SLAB_HALF
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WATERLOGGED
import org.sandboxpowered.silica.vanilla.util.Half

class SlabBlock(identifier: Identifier) : BaseBlock(identifier) {
    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, SLAB_HALF)
    }

    override fun createDefaultState(baseState: BlockState): BlockState = baseState.modify {
        WATERLOGGED set false
        SLAB_HALF set Half.BOTTOM
    }

    override fun getStateForPlacement(
        world: WorldReader,
        pos: Position,
        interaction: InteractionContext,
        ctx: EntityContext
    ): BlockState {
        val half =
            if (interaction.face != Direction.DOWN && (interaction.face == Direction.UP || interaction.cursor.y < 0.5))
                Half.BOTTOM
            else Half.TOP
        return defaultState.set(SLAB_HALF, half)
    }
}