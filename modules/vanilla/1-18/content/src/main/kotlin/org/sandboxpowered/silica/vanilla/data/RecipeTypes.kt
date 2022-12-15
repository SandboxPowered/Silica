package org.sandboxpowered.silica.vanilla.data

import org.sandboxpowered.silica.api.recipe.RecipeType
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.vanilla.recipe.*

object RecipeTypes {

    fun init() {
        register(RecipeType(SmeltingRecipe))
        register(RecipeType(BlastingRecipe))
        register(RecipeType(ShapedCraftingRecipe))
        register(RecipeType(ShapelessCraftingRecipe))
        register(RecipeType(StoneCuttingRecipe))
    }

    private fun register(recipeType: RecipeType) {
        Registries.RECIPE_TYPES.register(recipeType)
    }
}