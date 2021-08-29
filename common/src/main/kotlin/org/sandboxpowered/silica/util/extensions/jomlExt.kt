package org.sandboxpowered.silica.util.math

import org.joml.Vector3dc
import org.joml.Vector3fc
import org.joml.Vector3ic

operator fun Vector3dc.component1() = x()
operator fun Vector3dc.component2() = y()
operator fun Vector3dc.component3() = z()

operator fun Vector3fc.component1() = x()
operator fun Vector3fc.component2() = y()
operator fun Vector3fc.component3() = z()

operator fun Vector3ic.component1() = x()
operator fun Vector3ic.component2() = y()
operator fun Vector3ic.component3() = z()