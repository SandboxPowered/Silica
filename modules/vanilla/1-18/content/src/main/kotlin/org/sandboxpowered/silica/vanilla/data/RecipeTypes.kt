package org.sandboxpowered.silica.vanilla.data

import org.sandboxpowered.silica.api.recipe.RecipeType
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.vanilla.recipe.SmeltingRecipe
import org.sandboxpowered.utilities.Identifier

object RecipeTypes {

    fun init() {
        register(RecipeType<SmeltingRecipe>(Identifier("minecraft", "smelting")))
    }

    private fun register(recipeType: RecipeType) {
        Registries.RECIPE_TYPES.register(recipeType)
    }
}