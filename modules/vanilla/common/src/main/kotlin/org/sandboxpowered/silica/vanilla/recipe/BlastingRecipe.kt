package org.sandboxpowered.silica.vanilla.recipe

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.util.Identifying
import org.sandboxpowered.utilities.Identifier

class BlastingRecipe(
    identifier: Identifier,
    val group: String?,
    val ingredient: Ingredient,
    val result: ItemStack,
    val experience: Float,
    val cookingTime: Int
) : Recipe(identifier, Companion.identifier) {

    override val ingredientsHash: Int
        get() = TODO("Not yet implemented")

    companion object : Identifying<BlastingRecipe> {
        override val identifier = Identifier("minecraft", "blasting")
    }
}