package org.sandboxpowered.silica.content.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.block.Blocks;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.util.Direction;
import org.sandboxpowered.api.util.math.Position;
import org.sandboxpowered.api.world.World;

public class SnowBlock extends BaseBlock {
    public SnowBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockState createBaseState(BlockState baseState) {
        return baseState.with(Properties.SNOWY, false);
    }

    @Override
    public BlockState updateOnNeighborChanged(BlockState state, Direction direction, BlockState otherState, World world, Position position, Position otherPosition) {
        if (direction != Direction.UP) {
            return super.updateOnNeighborChanged(state, direction, otherState, world, position, otherPosition);
        } else {
            Block block = otherState.getBlock();
            return state.with(Properties.SNOWY, Blocks.SNOW_BLOCK.matches(block) || Blocks.SNOW.matches(block)); //TODO: Replace with snow tag
        }
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.SNOWY);
    }
}