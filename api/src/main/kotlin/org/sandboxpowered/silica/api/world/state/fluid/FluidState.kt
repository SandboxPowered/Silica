package org.sandboxpowered.silica.api.world.state.fluid

import com.google.common.collect.ImmutableMap
import org.sandboxpowered.silica.api.fluid.Fluid
import org.sandboxpowered.silica.api.world.state.BaseState
import org.sandboxpowered.silica.api.world.state.property.Property

class FluidState(base: Fluid, properties: ImmutableMap<Property<*>, Comparable<*>>) :
    BaseState<Fluid, FluidState>(base, properties) {
    fun getFluid(): Fluid = base
}