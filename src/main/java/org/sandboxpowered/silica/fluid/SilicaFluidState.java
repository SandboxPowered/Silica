package org.sandboxpowered.silica.fluid;

import com.google.common.collect.ImmutableMap;
import org.sandboxpowered.api.fluid.Fluid;
import org.sandboxpowered.api.state.FluidState;
import org.sandboxpowered.api.state.property.Property;
import org.sandboxpowered.silica.state.BaseState;

public class SilicaFluidState extends BaseState<Fluid, FluidState> implements FluidState {
    public SilicaFluidState(Fluid base, ImmutableMap<Property<?>, Comparable<?>> properties) {
        super(base, properties);
    }

    @Override
    public Fluid getFluid() {
        return base;
    }
}
