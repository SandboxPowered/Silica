package org.sandboxpowered.silica.content.inventory

import org.sandboxpowered.silica.api.item.ItemStack

class ResizableInventory : Inventory {
    override fun iterator(): Iterator<ItemStack> {
        TODO("Not yet implemented")
    }

    override val size: Int
        get() = TODO("Not yet implemented")

    override fun get(slot: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun set(slot: Int, stack: ItemStack): ItemStack {
        TODO("Not yet implemented")
    }
}