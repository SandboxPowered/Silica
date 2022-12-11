package org.sandboxpowered.silica.vanilla.recipe

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.util.Identifying
import org.sandboxpowered.utilities.Identifier

class ShapedCraftingRecipe(
    identifier: Identifier,
    val group: String?,
    val pattern: Array<String>,
    val key: Map<Char, Ingredient>,
    val result: ItemStack
) : Recipe(identifier, Companion.identifier) {

    override val ingredientsHash: Int
        get() = TODO("Not yet implemented")

    companion object : Identifying<ShapedCraftingRecipe> {
        override val identifier = Identifier("minecraft", "crafting_shaped")
    }
}