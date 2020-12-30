package org.sandboxpowered.silica.block;

import com.google.common.collect.ImmutableMap;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.property.Property;
import org.sandboxpowered.silica.state.BaseState;

public class SilicaBlockState extends BaseState<Block, BlockState> implements BlockState {
    public SilicaBlockState(Block base, ImmutableMap<Property<?>, Comparable<?>> properties) {
        super(base, properties);
    }

    @Override
    public Block getBlock() {
        return base;
    }
}
