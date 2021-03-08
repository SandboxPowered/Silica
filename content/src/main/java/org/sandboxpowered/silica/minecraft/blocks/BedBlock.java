package org.sandboxpowered.silica.minecraft.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.state.property.EnumProperty;

public class BedBlock extends BaseBlock {
    private static final EnumProperty<BedPart> BED_PART = EnumProperty.of("part", BedPart.class);
    public BedBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);

        builder.add(Properties.HORIZONTAL_FACING, Properties.OCCUPIED, BED_PART);
    }

    public static enum BedPart {
        HEAD,
        FOOT
    }
}
