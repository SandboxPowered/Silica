package org.sandboxpowered.silica.vanilla.recipe

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.silica.api.recipe.Recipe
import org.sandboxpowered.silica.api.recipe.ingredient.Ingredient
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.Identifying
import org.sandboxpowered.utilities.Identifier

class StoneCuttingRecipe(
    identifier: Identifier,
    group: String?,
    val ingredient: Ingredient,
    val result: ItemStack
) : Recipe(identifier, group, Companion.identifier) {

    constructor(
        identifier: Identifier,
        group: String?,
        ingredient: Ingredient,
        result: Identifier,
        count: Int
    ) : this(identifier, group, ingredient, ItemStack(Registries.ITEMS[result], count))

    override val ingredientsHash: Int
        get() = TODO("Not yet implemented")

    companion object : Identifying<StoneCuttingRecipe> {
        override val identifier = Identifier("minecraft", "stonecutting")
    }
}