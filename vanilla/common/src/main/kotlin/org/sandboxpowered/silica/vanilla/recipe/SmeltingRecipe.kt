package org.sandboxpowered.silica.vanilla.recipe

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.item.inventory.BaseInventory
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.util.Identifier

class SmeltingRecipe(
    val id: Identifier,
    val group: String?,
    val input: Ingredient,
    val output: ItemStack,
    val experience: Float
) : Recipe<BaseInventory> {
}