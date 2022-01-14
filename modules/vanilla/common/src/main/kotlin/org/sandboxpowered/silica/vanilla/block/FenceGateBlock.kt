package org.sandboxpowered.silica.vanilla.block

import org.joml.Vector3f
import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.entity.EntityContext
import org.sandboxpowered.silica.api.entity.InteractionContext
import org.sandboxpowered.silica.api.util.ActionResult
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.Hand
import org.sandboxpowered.utilities.Identifier
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.api.world.WorldReader
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.vanilla.block.BlockProperties.IN_WALL
import org.sandboxpowered.silica.vanilla.block.BlockProperties.OPEN
import org.sandboxpowered.silica.vanilla.block.BlockProperties.POWERED

class FenceGateBlock(identifier: Identifier) : BaseBlock(identifier) {

    override fun createDefaultState(baseState: BlockState): BlockState {
        return baseState.modify {
            OPEN set false
            POWERED set false
            IN_WALL set false
            HORIZONTAL_FACING set Direction.NORTH
        }
    }

    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(IN_WALL, HORIZONTAL_FACING, OPEN, POWERED)
    }

    override fun onUse(
        world: World,
        pos: Position,
        state: BlockState,
        hand: Hand,
        face: Direction,
        cursor: Vector3f,
        ctx: EntityContext
    ): ActionResult {
        if (ctx.sneaking) return ActionResult.PASS

        if (state[OPEN]) {
            world.setBlockState(pos, state.set(OPEN, false))
        } else {
            val facing = ctx.horizontalFacing
            var newState = state
            if (state[HORIZONTAL_FACING] == facing.opposite) {
                newState = newState.set(HORIZONTAL_FACING, facing)
            }
            world.setBlockState(pos, newState.set(OPEN, true))
        }
        return ActionResult.SUCCESS
    }

    override fun getStateForPlacement(
        world: WorldReader, pos: Position,
        interaction: InteractionContext, ctx: EntityContext
    ): BlockState =
        defaultState.set(HORIZONTAL_FACING, ctx.horizontalFacing)
}