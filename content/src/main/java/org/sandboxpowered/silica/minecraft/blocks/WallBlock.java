package org.sandboxpowered.silica.minecraft.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.block.FluidLoggable;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.state.property.EnumProperty;

public class WallBlock extends BaseBlock implements FluidLoggable {
    private static EnumProperty<WallType> NORTH = EnumProperty.of("north", WallType.class);
    private static EnumProperty<WallType> EAST = EnumProperty.of("east", WallType.class);
    private static EnumProperty<WallType> SOUTH = EnumProperty.of("south", WallType.class);
    private static EnumProperty<WallType> WEST = EnumProperty.of("west", WallType.class);

    public WallBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(NORTH, EAST, SOUTH, WEST, Properties.UP);
    }

    public static enum WallType {
        LOW,
        TALL,
        NONE
    }
}