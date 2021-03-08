package org.sandboxpowered.silica.minecraft.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.state.property.EnumProperty;

public class NoteBlock extends BaseBlock {
    private static final EnumProperty<Instrument> INSTRUMENT = EnumProperty.of("instrument", Instrument.class);

    public NoteBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(INSTRUMENT, Properties.NOTE, Properties.POWERED);
    }

    public static enum Instrument {
        HARP,
        BASEDRUM,
        SNARE,
        HAT,
        BASS,
        FLUTE,
        BELL,
        GUITAR,
        CHIME,
        XYLOPHONE,
        IRON_XYLOPHONE,
        COW_BELL,
        DIDGERIDOO,
        BIT,
        BANJO,
        PLING
    }
}
