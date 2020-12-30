package org.sandboxpowered.silica.content.blocks;

import org.sandboxpowered.api.block.Blocks;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.util.math.Position;
import org.sandboxpowered.api.world.World;
import org.sandboxpowered.silica.content.SilicaTags;

import java.util.Random;

public class SpreadingBlock extends SnowBlock {
    public SpreadingBlock(Settings settings) {
        super(settings);
    }

    private static boolean canSurvive(BlockState state, World world, Position pos) {
        Position abovePos = pos.up();
        BlockState aboveState = world.getBlockState(abovePos);
        if (aboveState.is(Blocks.SNOW) && aboveState.get(Properties.LAYERS) == 1) {
            return true;
        } else {
            return aboveState.isAir(); //TODO do a light check rather than air check
        }
    }

    @Override
    public void randomTick(World world, Position position, BlockState blockState, Random random) {
        if (!canSurvive(blockState, world, position)) {
            world.setBlockState(position, Blocks.DIRT.get());
        } else {
            BlockState blockState2 = getBaseState();

            Position.Mutable mutable = Position.Mutable.create();
            for (int i = 0; i < 4; ++i) {
                mutable.set(position);
                Position offsetPos = mutable.add(random.nextInt(3) - 1, random.nextInt(5) - 3, random.nextInt(3) - 1);
                BlockState offsetState = world.getBlockState(offsetPos);
                if (offsetState.isIn(SilicaTags.SPREADABLE_TARGET)) {
                    world.setBlockState(offsetPos, blockState2.with(Properties.SNOWY, world.getBlockState(offsetPos.up()).isIn(SilicaTags.SNOWY)));
                }
            }
        }
    }
}