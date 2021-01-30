package org.sandboxpowered.silica.content.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.shape.Shape;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.util.math.Position;
import org.sandboxpowered.api.world.WorldReader;

public class CarpetBlock extends BaseBlock {
    private static final Shape CARPET_SHAPE = Shape.cuboid(0, 0, 0, 16, 1, 16);
    private static final Shape CARPET_COLLISION_SHAPE = Shape.cuboid(0, -1, 0, 16, 0, 16);
    public CarpetBlock(Settings settings) {
        super(settings);
    }

    @Override
    public Shape getShape(WorldReader reader, Position position, BlockState state) {
        return CARPET_SHAPE;
    }

    @Override
    public Shape getCollisionShape(WorldReader reader, Position position, BlockState state) {
        return CARPET_COLLISION_SHAPE;
    }
}