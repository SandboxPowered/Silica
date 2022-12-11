package org.sandboxpowered.silica.api.recipe.ingredient

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.utilities.Identifier

sealed class Ingredient {
    abstract fun matches(stack: ItemStack): Boolean

    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int

    class Item(val item: Identifier) : Ingredient() {
        override fun matches(stack: ItemStack) = stack.item.identifier == item

        override fun equals(other: Any?): Boolean = TODO("Not yet implemented")
        override fun hashCode(): Int = TODO("Not yet implemented")
    }

    class Tag(val tag: Identifier) : Ingredient() {
        override fun matches(stack: ItemStack) = TODO("Not implemented")
        override fun equals(other: Any?): Boolean = TODO("Not yet implemented")
        override fun hashCode(): Int = TODO("Not yet implemented")
    }

    // FIXME : can not deserialize this atm
    class Composite(val ingredients: Collection<Ingredient>) : Ingredient() {
        override fun matches(stack: ItemStack) = ingredients.any { it.matches(stack) }

        override fun equals(other: Any?): Boolean = TODO("Not yet implemented")
        override fun hashCode(): Int = TODO("Not yet implemented")
    }
}
