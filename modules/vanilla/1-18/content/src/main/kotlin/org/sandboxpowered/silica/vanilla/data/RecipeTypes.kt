package org.sandboxpowered.silica.vanilla.data

import org.sandboxpowered.silica.api.recipe.RecipeType
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.vanilla.recipe.ShapedCraftingRecipe
import org.sandboxpowered.silica.vanilla.recipe.SmeltingRecipe

object RecipeTypes {

    fun init() {
        register(RecipeType(SmeltingRecipe))
        register(RecipeType(ShapedCraftingRecipe))
    }

    private fun register(recipeType: RecipeType) {
        Registries.RECIPE_TYPES.register(recipeType)
    }
}