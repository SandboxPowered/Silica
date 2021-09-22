package org.sandboxpowered.silica.world.state.fluid

import com.google.common.collect.ImmutableMap
import org.sandboxpowered.silica.content.fluid.Fluid
import org.sandboxpowered.silica.world.state.BaseState
import org.sandboxpowered.silica.world.state.property.Property

class FluidState(base: Fluid, properties: ImmutableMap<Property<*>, Comparable<*>>) :
    BaseState<Fluid, FluidState>(base, properties) {
    fun getFluid(): Fluid = base
}