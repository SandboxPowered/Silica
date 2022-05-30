package org.sandboxpowered.silica.api.recipe.ingredient

import org.sandboxpowered.silica.api.item.ItemStack
import org.sandboxpowered.utilities.Identifier
import java.util.*

sealed class Ingredient(@Transient private val identifier: Identifier) {
    abstract fun matches(stack: ItemStack): Boolean

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if (this === other) return true
        if (other.javaClass != this.javaClass) return false

        if (identifier != (other as Ingredient).identifier) return false

        return true
    }

    // TODO: sort this out
    override fun hashCode(): Int = Objects.hash(javaClass.name, identifier)

    class Item(val item: Identifier) : Ingredient(item) {
        override fun matches(stack: ItemStack) = stack.item.identifier == item
    }

    class Tag(val tag: Identifier) : Ingredient(tag) {
        override fun matches(stack: ItemStack) = TODO("Not implemented")
    }
}
