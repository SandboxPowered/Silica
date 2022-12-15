package org.sandboxpowered.silica.api.recipe

import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.Identifying

class RecipeType(
    override val identifier: Identifier,
    val recipeClass: Class<out Recipe>
) : RegistryEntry<RecipeType> {
    override val registry: Registry<RecipeType> get() = Registries.RECIPE_TYPES

    companion object {
        inline operator fun <reified T : Recipe> invoke(identifier: Identifier) = RecipeType(identifier, T::class.java)
        inline operator fun <reified R : Recipe> invoke(identifying: Identifying<out R>) =
            RecipeType(identifying.identifier, R::class.java)
    }
}