package org.sandboxpowered.silica.vanilla.recipe

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.utilities.Identifier

class SmeltingRecipe(
    identifier: Identifier,
    val group: String?,
    val ingredient: Ingredient,
    val result: ItemStack,
    val experience: Float,
    val cookingTime: Int
) : Recipe(identifier, typeIdentifier) {

    override val ingredientsHash: Int
        get() = TODO("Not yet implemented")

    private companion object {
        private val typeIdentifier = Identifier("minecraft", "smelting")
    }
}