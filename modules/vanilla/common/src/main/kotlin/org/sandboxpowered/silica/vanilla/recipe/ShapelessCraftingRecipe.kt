package org.sandboxpowered.silica.vanilla.recipe

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.Identifying

class ShapelessCraftingRecipe(
    identifier: Identifier,
    group: String?,
    val ingredients: Collection<Ingredient>,
    val result: ItemStack
) : Recipe(identifier, group, Companion.identifier) {

    override val ingredientsHash: Int
        get() = TODO("Not yet implemented")

    companion object : Identifying<ShapelessCraftingRecipe> {
        override val identifier = Identifier("minecraft", "crafting_shapeless")
    }
}