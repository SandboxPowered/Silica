package org.sandboxpowered.silica.content.inventory

import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.content.item.ItemStack

class PlayerInventory : Inventory {
    companion object {
        const val HOTBAR_SIZE = 9
        const val MAIN_SIZE = 36
        const val ARMOUR_SIZE = 4
    }

    val main = ArrayList<ItemStack>(MAIN_SIZE).apply { fill(ItemStack.EMPTY) }
    val armour = ArrayList<ItemStack>(ARMOUR_SIZE).apply { fill(ItemStack.EMPTY) }
    val offHand = ItemStack.EMPTY

    var selectedSlot: Int = 0

    val mainHandStack: ItemStack
        get() = if (isValidHotbarIndex(selectedSlot)) main[selectedSlot] else ItemStack.EMPTY

    private fun isValidHotbarIndex(slot: Int): Boolean = slot in 0 until HOTBAR_SIZE

    override val size: Int
        get() = main.size + armour.size + 1

    override val isEmpty: Boolean
        get() = TODO("Not yet implemented")

    override fun get(slot: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun set(slot: Int, stack: ItemStack) {
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