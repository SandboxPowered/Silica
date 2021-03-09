package org.sandboxpowered.silica.minecraft.blocks;

import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.ecs.Entity;
import org.sandboxpowered.api.item.ItemStack;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.Properties;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.util.Direction;
import org.sandboxpowered.api.util.Hand;
import org.sandboxpowered.api.util.math.Position;
import org.sandboxpowered.api.util.math.Vec3d;
import org.sandboxpowered.api.world.BlockFlag;
import org.sandboxpowered.api.world.World;
import org.sandboxpowered.api.world.WorldReader;

import java.util.Random;

public class RedstoneLightBlock extends BaseBlock {
    public RedstoneLightBlock(Settings settings) {
        super(settings);
    }

    @Override
    public void appendProperties(StateFactory.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(Properties.LIT);
    }

    @Override
    public BlockState getStateForPlacement(WorldReader reader, Position pos, Entity player, Hand hand, ItemStack stack, Direction side, Vec3d hitPos) {
        return getBaseState().with(Properties.LIT, reader.hasNeighborSignal(pos));
    }

    @Override
    public void scheduledTick(World serverWorld, Position position, BlockState blockState, Random random) {
        if (blockState.get(Properties.LIT) && !serverWorld.hasNeighborSignal(position)) {
            serverWorld.setBlockState(position, blockState.cycle(Properties.LIT), BlockFlag.SEND_TO_CLIENT);
        }
    }

    @Override
    public void onNeighborChanged(BlockState state, World world, Position position, Block other, Position otherPosition) {
        if (world.isServer()) {
            boolean lit = state.get(Properties.LIT);
            if (lit != world.hasNeighborSignal(position)) {
                if (lit) world.scheduleTick(position, this, 4);
                else world.setBlockState(position, state.cycle(Properties.LIT));
            }
        }
    }
}