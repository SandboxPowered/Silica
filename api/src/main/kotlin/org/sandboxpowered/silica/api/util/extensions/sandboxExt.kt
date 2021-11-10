package org.sandboxpowered.silica.api.util.extensions

import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.math.Position

operator fun Position.component1(): Int = x
operator fun Position.component2(): Int = y
operator fun Position.component3(): Int = z

operator fun Identifier.component1(): String = namespace
operator fun Identifier.component2(): String = path