package org.sandboxpowered.silica.content.inventory

import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.item.ItemStack
import kotlin.math.min

interface Inventory : Iterable<ItemStack> {
    /**
     * The amount of slots in this inventory
     */
    val size: Int

    /**
     * Whether this inventory is empty
     */
    val isEmpty: Boolean get() = all(ItemStack::isEmpty)

    /**
     * Get stack in [slot]
     */
    operator fun get(slot: Int): ItemStack

    /**
     * Set [stack] in [slot]
     */
    operator fun set(slot: Int, stack: ItemStack): ItemStack

    /**
     * Clear this inventory
     */
    fun clear() = repeat(size) { set(it, ItemStack.EMPTY) }

    /**
     * Add the [stack] to given [slot] and return the remainder
     * In case the items do not match, the full [stack] will be returned
     */
    fun add(slot: Int, stack: ItemStack): ItemStack = get(slot).merge(stack)

    /**
     * Remove the stack in [slot] and return it
     */
    fun removeStack(slot: Int): ItemStack = set(slot, ItemStack.EMPTY)

    /**
     * Remove at least [amount] items from the stack in [slot] and return the removed stack
     */
    fun removeStack(slot: Int, amount: Int): ItemStack {
        val stack = get(slot)
        val toRemove = min(amount, stack.count)
        set(slot, stack.duplicate().apply { this -= toRemove })
        stack.count = toRemove
        return stack
    }

    /**
     * Whether the stack in [slot] can merge with [stack]
     */
    fun isValidItem(slot: Int, stack: ItemStack): Boolean = get(slot).canMerge(stack)

    /**
     * Count the amount of [item] in this inventory
     */
    fun count(item: Item): Int = asSequence().filter { it.isItemEqual(item) }.sumOf { it.count }

    /**
     * Whether this inventory contains any [item]
     */
    operator fun contains(item: Item): Boolean = any { it.isItemEqual(item) }

    /**
     * Whether this inventory contains any of the [items]
     */
    fun containsAny(items: Set<Item>): Boolean = any { !it.isEmpty && it.item in items }
}