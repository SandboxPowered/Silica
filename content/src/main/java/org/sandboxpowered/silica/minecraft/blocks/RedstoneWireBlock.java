package org.sandboxpowered.silica.minecraft.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.state.property.EnumProperty;

public class RedstoneWireBlock extends BaseBlock {
    private static final EnumProperty<Side> NORTH = EnumProperty.of("north", Side.class);
    private static final EnumProperty<Side> EAST = EnumProperty.of("east", Side.class);
    private static final EnumProperty<Side> SOUTH = EnumProperty.of("south", Side.class);
    private static final EnumProperty<Side> WEST = EnumProperty.of("west", Side.class);

    public RedstoneWireBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.POWER, NORTH, EAST, SOUTH, WEST);
    }

    public static enum Side {
        UP,
        SIDE,
        NONE
    }
}
