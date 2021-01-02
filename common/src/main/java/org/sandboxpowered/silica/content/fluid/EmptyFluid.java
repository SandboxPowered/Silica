package org.sandboxpowered.silica.content.fluid;

import org.sandboxpowered.api.block.Blocks;
import org.sandboxpowered.api.fluid.Fluid;
import org.sandboxpowered.api.item.Item;
import org.sandboxpowered.api.item.Items;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.FluidState;
import org.sandboxpowered.api.state.StateFactory;
import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.api.util.math.Position;
import org.sandboxpowered.api.util.math.Vec3d;
import org.sandboxpowered.api.world.WorldReader;

import java.util.Optional;

public class EmptyFluid implements Fluid {
    private StateFactory<Fluid, FluidState> stateFactory;

    @Override
    public FluidState getBaseState() {
        return stateFactory.getBaseState();
    }

    @Override
    public StateFactory<Fluid, FluidState> getStateFactory() {
        return stateFactory;
    }

    @Override
    public boolean isStill(FluidState state) {
        return true;
    }

    @Override
    public BlockState asBlockState(FluidState state) {
        return Blocks.AIR.get().getBaseState();
    }

    @Override
    public Fluid asStill() {
        return this;
    }

    @Override
    public Fluid asFlowing() {
        return this;
    }

    @Override
    public boolean isInfinite() {
        return false;
    }

    @Override
    public int getLevel(FluidState fluidState) {
        return 0;
    }

    @Override
    public Item asBucket() {
        return Items.AIR.get();
    }

    private Identity identity;

    @Override
    public Identity getIdentity() {
        return identity;
    }

    @Override
    public Fluid setIdentity(Identity identity) {
        this.identity = identity;
        return this;
    }

    @Override
    public Optional<Item> asItem() {
        return Optional.empty();
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean matches(Fluid fluid) {
        return fluid.isEmpty();
    }

    @Override
    public FluidState asStill(boolean falling) {
        return getBaseState();
    }

    @Override
    public FluidState asFlowing(int level, boolean falling) {
        return getBaseState();
    }

    @Override
    public int getTickRate(WorldReader world) {
        return 0;
    }

    @Override
    public Optional<Vec3d> getVelocity(WorldReader world, Position position, FluidState state) {
        return Optional.empty();
    }
}