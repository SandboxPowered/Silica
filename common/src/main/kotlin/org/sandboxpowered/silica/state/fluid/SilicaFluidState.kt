package org.sandboxpowered.silica.state.fluid

import com.google.common.collect.ImmutableMap
import org.sandboxpowered.api.fluid.Fluid
import org.sandboxpowered.api.world.state.FluidState
import org.sandboxpowered.api.world.state.Property
import org.sandboxpowered.silica.state.BaseState

class SilicaFluidState(base: Fluid, properties: ImmutableMap<Property<*>, Comparable<*>>) :
    BaseState<Fluid, FluidState>(base, properties), FluidState {
    override fun getFluid(): Fluid {
        return base
    }
}