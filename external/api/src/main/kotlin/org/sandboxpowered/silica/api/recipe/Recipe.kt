package org.sandboxpowered.silica.api.recipe

import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.utilities.Identifier

abstract class Recipe(
    override val identifier: Identifier,
    val group: String?,
    val type: Identifier
) : RegistryEntry<Recipe> {
    abstract val ingredientsHash: Int

    override val registry: Registry<Recipe> get() = Registries.RECIPES
}