package org.sandboxpowered.silica.content.inventory

import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.content.item.ItemStack

interface Inventory {
    val size: Int
    val isEmpty: Boolean

    operator fun get(slot: Int): ItemStack
    operator fun set(slot: Int, stack: ItemStack)

    fun removeStack(slot: Int): ItemStack
    fun removeStack(slot: Int, amount: Int): ItemStack

    fun isValidItem(slot: Int, item: ItemStack): Boolean

    fun count(item: Item): Int

    operator fun contains(item: Item): Boolean

    fun containsAny(vararg items: Item): Boolean
}