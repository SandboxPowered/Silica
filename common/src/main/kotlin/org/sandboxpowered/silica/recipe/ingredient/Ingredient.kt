package org.sandboxpowered.silica.recipe.ingredient

import kotlinx.serialization.json.*
import org.sandboxpowered.silica.content.item.ItemStack
import java.util.*
import java.util.function.Predicate

class Ingredient(val values: Array<Value>) : Predicate<ItemStack> {
    private var internalStacks: Array<ItemStack>? = null
    val stacks: Array<ItemStack>
        get() {
            reify()
            return internalStacks ?: emptyArray()
        }

    override fun test(t: ItemStack): Boolean = if (stacks.isEmpty()) t.isEmpty else stacks.any { it == t }

    private fun reify() {
        if (internalStacks == null) {
            internalStacks = values.flatMap(Value::items).distinct().toTypedArray()
        }
    }

    fun toJson(): JsonElement = if (values.size == 1) values[0].serialize() else buildJsonArray {
        values.forEach {
            add(it.serialize())
        }
    }

    sealed interface Value {
        val items: Collection<ItemStack>

        fun serialize(): JsonObject
    }

    data class ItemValue(val item: ItemStack) : Value {
        override val items: Collection<ItemStack> = Collections.singleton(item)

        override fun serialize(): JsonObject {
            return buildJsonObject {
                put("item", item.item.identifier.toString())
            }
        }
    }
}