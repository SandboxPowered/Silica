package org.sandboxpowered.silica.vanilla.recipe

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.Identifying

abstract class SimpleProcessingRecipe(
    identifier: Identifier,
    group: String?,
    val ingredient: Ingredient,
    val result: ItemStack,
    val experience: Float,
    val cookingTime: Int,
    type: Identifier
) : Recipe(identifier, group, type) {
    override val ingredientsHash: Int
        get() = TODO("Not yet implemented")
}

class SmeltingRecipe(
    identifier: Identifier,
    group: String?,
    ingredient: Ingredient,
    result: ItemStack,
    experience: Float,
    cookingTime: Int
) : SimpleProcessingRecipe(identifier, group, ingredient, result, experience, cookingTime, Companion.identifier) {
    companion object : Identifying<SimpleProcessingRecipe> {
        override val identifier = Identifier("minecraft", "smelting")
    }
}

class BlastingRecipe(
    identifier: Identifier,
    group: String?,
    ingredient: Ingredient,
    result: ItemStack,
    experience: Float,
    cookingTime: Int
) : SimpleProcessingRecipe(identifier, group, ingredient, result, experience, cookingTime, Companion.identifier) {
    companion object : Identifying<BlastingRecipe> {
        override val identifier = Identifier("minecraft", "blasting")
    }
}
