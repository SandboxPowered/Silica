package org.sandboxpowered.silica.world;

import org.jetbrains.annotations.Nullable;
import org.sandboxpowered.api.block.Blocks;
import org.sandboxpowered.api.block.entity.BlockEntity;
import org.sandboxpowered.api.entity.Entity;
import org.sandboxpowered.api.item.ItemStack;
import org.sandboxpowered.api.shape.Box;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.FluidState;
import org.sandboxpowered.api.tags.TagManager;
import org.sandboxpowered.api.util.Side;
import org.sandboxpowered.api.util.math.Position;
import org.sandboxpowered.api.world.BlockFlag;
import org.sandboxpowered.api.world.World;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class SilicaWorld implements World {
    private final Map<Position, BlockState> stateMap = new HashMap<>();

    private final BlockState defaultState = Blocks.AIR.get().getBaseState();

    private com.artemis.World artemisWorld;

    @Override
    public BlockState getBlockState(Position position) {
        return stateMap.getOrDefault(position, defaultState);
    }

    @Override
    public boolean setBlockState(Position position, BlockState state, BlockFlag... flags) {
        stateMap.put(position, state);
        return true;
    }

    @Override
    public Side getSide() {
        return null;
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(Position position) {
        return null;
    }

    @Override
    public FluidState getFluidState(Position position) {
        return null;
    }

    @Override
    public Stream<Entity> getEntitiesWithin(Box box) {
        return Stream.empty();
    }

    @Override
    public <T extends Entity> Stream<T> getEntitiesWithin(Box box, Class<T> filter) {
        return Stream.empty();
    }

    @Override
    public long getWorldTime() {
        return 0;
    }

    @Override
    public TagManager getTagManager() {
        return null;
    }

    @Override
    public void spawnItem(double x, double y, double z, ItemStack stack) {

    }
}
