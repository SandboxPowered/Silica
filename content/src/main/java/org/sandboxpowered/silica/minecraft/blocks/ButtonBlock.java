package org.sandboxpowered.silica.minecraft.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.state.property.EnumProperty;

public class ButtonBlock extends BaseBlock {
    private final EnumProperty<AttachFace> FACE = EnumProperty.of("face", AttachFace.class);

    public ButtonBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.HORIZONTAL_FACING, Properties.POWERED, FACE);
    }

    public enum AttachFace {
        FLOOR,
        WALL,
        CEILING
    }

}
