package org.sandboxpowered.silica.content.inventory

import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.content.item.ItemStack

class ResizableInventory : Inventory {
    override val size: Int
        get() = TODO("Not yet implemented")
    override val isEmpty: Boolean
        get() = TODO("Not yet implemented")

    fun reset(size: Int) {
        TODO("Not yet implemented")
    }

    override fun get(slot: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun set(slot: Int, stack: ItemStack) {
        TODO("Not yet implemented")
    }

    override fun add(i: Int, stack: ItemStack) {
        TODO("Not yet implemented")
    }

    override fun removeStack(slot: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun isValidItem(slot: Int, item: ItemStack): Boolean {
        TODO("Not yet implemented")
    }

    override fun count(item: Item): Int {
        TODO("Not yet implemented")
    }

    override fun contains(item: Item): Boolean {
        TODO("Not yet implemented")
    }

    override fun containsAny(vararg items: Item): Boolean {
        TODO("Not yet implemented")
    }
}