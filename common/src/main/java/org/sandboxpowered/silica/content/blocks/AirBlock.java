package org.sandboxpowered.silica.content.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.shape.Shape;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.util.math.Position;
import org.sandboxpowered.api.world.WorldReader;

public class AirBlock extends BaseBlock {
    public AirBlock(Settings settings) {
        super(settings);
    }

    @Override
    public boolean isAir(BlockState state) {
        return true;
    }

    @Override
    public RenderType getRenderType() {
        return RenderType.INVISIBLE;
    }

    @Override
    public Shape getShape(WorldReader reader, Position position, BlockState state) {
        return Shape.empty();
    }
}
