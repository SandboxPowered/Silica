package org.sandboxpowered.silica.content.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.util.Direction;

public class AxisBlock extends BaseBlock {
    public AxisBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockState createBaseState(BlockState baseState) {
        return baseState.with(Properties.AXIS, Direction.Axis.Y);
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.AXIS);
    }
}