package org.sandboxpowered.silica.recipe

import org.sandboxpowered.silica.content.inventory.BaseInventory
import org.sandboxpowered.silica.content.item.ItemStack
import org.sandboxpowered.silica.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.util.Identifier

class SmeltingRecipe(
    val id: Identifier,
    val group: String?,
    val input: Ingredient,
    val output: ItemStack,
    val experience: Float
) : Recipe<BaseInventory> {
}