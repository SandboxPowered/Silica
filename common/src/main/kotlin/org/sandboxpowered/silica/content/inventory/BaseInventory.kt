package org.sandboxpowered.silica.content.inventory

import org.sandboxpowered.silica.content.item.ItemStack

open class BaseInventory : Inventory {

    private val slots = mutableListOf<ItemStack>()

    fun section(size: Int, init: (Int) -> ItemStack = { ItemStack.EMPTY }): Inventory {
        val currentSize = slots.size
        slots.addAll(Array(size, init))
        return Section(currentSize, size, slots)
    }

    override fun clear() = slots.replaceAll { ItemStack.EMPTY }
    override val size: Int get() = slots.size
    override fun get(slot: Int) = slots[slot]
    override fun set(slot: Int, stack: ItemStack) = slots.set(slot, stack)
    override fun iterator(): Iterator<ItemStack> = slots.iterator()

    private class Section(
        private val fromIndex: Int,
        override val size: Int,
        private val backingSlots: MutableList<ItemStack>
    ) : Inventory {
        override operator fun get(slot: Int): ItemStack {
            checkIndex(slot)
            return backingSlots[slot + fromIndex]
        }

        override operator fun set(slot: Int, stack: ItemStack): ItemStack {
            checkIndex(slot)
            return backingSlots.set(slot + fromIndex, stack)
        }

        override fun iterator(): Iterator<ItemStack> = SectionIterator()

        private fun checkIndex(index: Int) = require(index in 0 until size) { "Index $index out of bounds" }

        private inner class SectionIterator : Iterator<ItemStack> {
            private var index = 0

            override fun hasNext() = index < size - 1
            override fun next(): ItemStack = get(index++)
        }
    }
}