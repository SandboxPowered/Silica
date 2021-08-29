package org.sandboxpowered.silica.util

import org.sandboxpowered.silica.util.math.Position

operator fun Position.component1(): Int = getX()
operator fun Position.component2(): Int = getY()
operator fun Position.component3(): Int = getZ()

operator fun Identifier.component1(): String = namespace
operator fun Identifier.component2(): String = path