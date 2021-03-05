package org.sandboxpowered.silica.minecraft.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.block.FluidLoggable;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.state.StateFactory;

public class GlassPaneBlock extends BaseBlock implements FluidLoggable {
    public GlassPaneBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected BlockState createBaseState(BlockState baseState) {
        return baseState;
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.NORTH);
        builder.add(Properties.EAST);
        builder.add(Properties.SOUTH);
        builder.add(Properties.WEST);
    }
}
