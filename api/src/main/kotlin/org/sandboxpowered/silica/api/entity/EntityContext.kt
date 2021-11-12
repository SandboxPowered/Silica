package org.sandboxpowered.silica.api.entity

import org.sandboxpowered.silica.api.util.Direction

interface EntityContext {
    val sneaking: Boolean
    val horizontalFacing: Direction
}