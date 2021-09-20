package org.sandboxpowered.silica.state.fluid

import com.google.common.collect.ImmutableMap
import org.sandboxpowered.silica.fluid.Fluid
import org.sandboxpowered.silica.state.BaseState
import org.sandboxpowered.silica.state.property.Property

class FluidState(base: Fluid, properties: ImmutableMap<Property<*>, Comparable<*>>) :
    BaseState<Fluid, FluidState>(base, properties) {
    val isAir: Boolean = base.isAir(this)

    fun getFluid(): Fluid = base
}