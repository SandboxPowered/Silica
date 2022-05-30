package org.sandboxpowered.silica.api.recipe

import org.sandboxpowered.utilities.Identifier

abstract class Recipe(
    val identifier: Identifier,
    val type: Identifier
) {
    abstract val ingredientsHash: Int
}