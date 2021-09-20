package org.sandboxpowered.silica.fluid

import org.sandboxpowered.silica.registry.RegistryEntry
import org.sandboxpowered.silica.state.fluid.FluidState

sealed interface Fluid : RegistryEntry<Fluid> {
    fun isAir(state: FluidState): Boolean
}