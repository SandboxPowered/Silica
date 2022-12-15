package org.sandboxpowered.silica.vanilla.block

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.entity.EntityContext
import org.sandboxpowered.silica.api.entity.InteractionContext
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.Identifier
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.api.world.WorldReader
import org.sandboxpowered.silica.api.world.state.StateProvider
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.block.BlockProperties.HALF
import org.sandboxpowered.silica.vanilla.block.BlockProperties.HORIZONTAL_FACING
import org.sandboxpowered.silica.vanilla.block.BlockProperties.STAIR_SHAPE
import org.sandboxpowered.silica.vanilla.block.BlockProperties.WATERLOGGED
import org.sandboxpowered.silica.vanilla.util.Half
import org.sandboxpowered.silica.vanilla.util.StairShape

class StairBlock(identifier: Identifier) : BaseBlock(identifier) {

    private fun canTakeShape(world: WorldReader, pos: Position, state: BlockState, direction: Direction): Boolean {
        val offsetState = world.getBlockState(pos.shift(direction))
        return offsetState.block !is StairBlock || !state.matches(offsetState, HORIZONTAL_FACING, HALF)
    }

    private fun getStairShape(world: WorldReader, pos: Position, state: BlockState): StairShape {
        val facing = state[HORIZONTAL_FACING]
        val nextState = world.getBlockState(pos.shift(facing))
        if (nextState.block is StairBlock && state.matches(nextState, HALF)) {
            val otherFacing = nextState[HORIZONTAL_FACING]
            if (facing.axis != otherFacing.axis && canTakeShape(world, pos, state, otherFacing.opposite)) {
                return if (facing.counterClockWise == otherFacing) StairShape.OUTER_LEFT else StairShape.OUTER_RIGHT
            }
        }

        val oppositeState = world.getBlockState(pos.shift(facing.opposite))
        if (oppositeState.block is StairBlock && state.matches(oppositeState, HALF)) {
            val otherFacing = oppositeState[HORIZONTAL_FACING]
            if (facing.axis != otherFacing.axis && canTakeShape(world, pos, state, otherFacing)) {
                return if (facing.counterClockWise == otherFacing) StairShape.INNER_LEFT else StairShape.INNER_RIGHT
            }
        }

        return StairShape.STRAIGHT
    }

    override fun createDefaultState(baseState: BlockState): BlockState = baseState.modify {
        HALF set Half.BOTTOM
        HORIZONTAL_FACING set Direction.NORTH
        WATERLOGGED set false
        STAIR_SHAPE set StairShape.STRAIGHT
    }

    override fun appendProperties(builder: StateProvider.Builder<Block, BlockState>) {
        builder.add(WATERLOGGED, HORIZONTAL_FACING, HALF, STAIR_SHAPE)
    }

    override fun onNeighborUpdate(
        world: World,
        pos: Position,
        state: BlockState,
        origin: Position,
        originState: BlockState,
        side: Direction
    ) {
        val wantedShape = getStairShape(world, pos, state)
        if (wantedShape != state[STAIR_SHAPE]) world.setBlockState(pos, state.set(STAIR_SHAPE, wantedShape))
    }

    override fun getStateForPlacement(
        world: WorldReader, pos: Position,
        interaction: InteractionContext, ctx: EntityContext
    ): BlockState {
        val half =
            if (interaction.face != Direction.DOWN && (interaction.face == Direction.UP || interaction.cursor.y < 0.5))
                Half.BOTTOM
            else Half.TOP
        val state = defaultState.set(HORIZONTAL_FACING, ctx.horizontalFacing).set(HALF, half)
        return state.set(STAIR_SHAPE, getStairShape(world, pos, state))
    }
}