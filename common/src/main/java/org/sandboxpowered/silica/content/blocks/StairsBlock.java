package org.sandboxpowered.silica.content.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.block.FluidLoggable;
import org.sandboxpowered.api.entity.player.Hand;
import org.sandboxpowered.api.entity.player.PlayerEntity;
import org.sandboxpowered.api.fluid.Fluids;
import org.sandboxpowered.api.item.ItemStack;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.FluidState;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.tags.BlockTags;
import org.sandboxpowered.api.util.Direction;
import org.sandboxpowered.api.util.Half;
import org.sandboxpowered.api.util.StairShape;
import org.sandboxpowered.api.util.math.Position;
import org.sandboxpowered.api.util.math.Vec3d;
import org.sandboxpowered.api.world.WorldReader;

import static org.sandboxpowered.api.state.Properties.*;

/**
 * Will be put into SandboxAPI as a default type once fully implemented
 */
public class StairsBlock extends BaseBlock implements FluidLoggable {
    public StairsBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(STAIR_SHAPE, HALF, HORIZONTAL_FACING);
    }

    @Override
    public BlockState getStateForPlacement(WorldReader reader, Position pos, PlayerEntity player, Hand hand, ItemStack stack, Direction side, Vec3d hitPos) {
        FluidState fluidState = reader.getFluidState(pos);
        BlockState state = getBaseState()
                .with(WATERLOGGED, Fluids.WATER.matches(fluidState.getFluid()))
                .with(HORIZONTAL_FACING, player.getHorizontalFacing())
                .with(HALF, side == Direction.DOWN || (side != Direction.UP && hitPos.getY() - pos.getY() > 0.5f) ? Half.TOP : Half.BOTTOM);
        return state.with(STAIR_SHAPE, getStairShape(state, reader, pos));
    }

    private StairShape getStairShape(BlockState state, WorldReader reader, Position pos) {
        Direction direction = state.get(HORIZONTAL_FACING);
        BlockState offsetState = reader.getBlockState(pos.offset(direction));
        if (isInStairTag(offsetState) && state.get(HALF) == offsetState.get(HALF)) {
            Direction offsetDir = offsetState.get(FACING);
            if (offsetDir.getAxis() != state.get(FACING).getAxis() && shouldConsiderStair(state, reader, pos, offsetDir.getOppositeDirection())) {
                if (offsetDir == direction.rotateYCounterClockwise()) {
                    return StairShape.OUTER_LEFT;
                }
                return StairShape.OUTER_RIGHT;
            }
        }
        offsetState = reader.getBlockState(pos.offset(direction.getOppositeDirection()));
        if (isInStairTag(offsetState) && offsetState.get(HALF) == offsetState.get(HALF)) {
            Direction offsetDir = offsetState.get(FACING);
            if (offsetDir.getAxis() != state.get(FACING).getAxis() && shouldConsiderStair(state, reader, pos, offsetDir)) {
                if (offsetDir == direction.rotateYCounterClockwise()) {
                    return StairShape.INNER_LEFT;
                }
                return StairShape.INNER_RIGHT;
            }
        }
        return StairShape.STRAIGHT;
    }

    private boolean shouldConsiderStair(BlockState state, WorldReader reader, Position pos, Direction direction) {
        BlockState blockState2 = reader.getBlockState(pos.offset(direction));
        return !isInStairTag(blockState2) || blockState2.get(FACING) != state.get(FACING) || blockState2.get(HALF) != state.get(HALF);
    }

    public static boolean isInStairTag(BlockState blockState) {
        return blockState.isIn(BlockTags.STAIRS);
    }
}