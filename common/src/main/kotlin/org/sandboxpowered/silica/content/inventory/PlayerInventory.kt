package org.sandboxpowered.silica.content.inventory

import org.sandboxpowered.silica.content.item.Item
import org.sandboxpowered.silica.content.item.ItemStack
import org.sandboxpowered.silica.registry.SilicaRegistries.items
import org.sandboxpowered.silica.content.item.ItemStack.Companion.EMPTY as EMPTY_STACK

class PlayerInventory : Inventory {
    companion object {
        const val HOTBAR_SIZE = 9
        const val MAIN_SIZE = 36
        const val ARMOUR_SIZE = 4

        val STONE by items().guaranteed
    }

    val main = ArrayList<ItemStack>(MAIN_SIZE).apply {
        this.ensureCapacity(MAIN_SIZE)
        for (i in 0 until MAIN_SIZE)
            add(i, ItemStack(STONE, i))
    }
    val armour = ArrayList<ItemStack>(ARMOUR_SIZE).apply {
        this.ensureCapacity(ARMOUR_SIZE)
        fill(EMPTY_STACK)
    }
    var offHand = EMPTY_STACK

    var selectedSlot: Int = 0

    fun reset() {
        selectedSlot = 0
        main.fill(EMPTY_STACK)
        armour.fill(EMPTY_STACK)
        offHand = EMPTY_STACK
    }

    val mainHandStack: ItemStack
        get() = if (isValidHotbarIndex(selectedSlot)) main[selectedSlot] else EMPTY_STACK

    private fun isValidHotbarIndex(slot: Int): Boolean = slot in 0 until HOTBAR_SIZE

    override val size: Int
        get() = main.size + armour.size + 1

    override val isEmpty: Boolean
        get() = main.all { it.isEmpty } && armour.all { it.isEmpty } && offHand.isEmpty

    override fun get(slot: Int): ItemStack = when (slot) {
        in 0 until MAIN_SIZE -> main[slot]
        in MAIN_SIZE until (MAIN_SIZE + ARMOUR_SIZE) -> armour[slot - MAIN_SIZE]
        MAIN_SIZE + ARMOUR_SIZE -> offHand
        else -> EMPTY_STACK
    }

    override fun set(slot: Int, stack: ItemStack) {
        when (slot) {
            in 0 until MAIN_SIZE -> main[slot] = stack
            in MAIN_SIZE until MAIN_SIZE + ARMOUR_SIZE -> armour[slot - MAIN_SIZE] = stack
            MAIN_SIZE + ARMOUR_SIZE -> offHand = stack
        }
    }

    override fun add(i: Int, stack: ItemStack) {
        TODO("Not yet implemented")
    }

    override fun removeStack(slot: Int): ItemStack {
        when (slot) {
            in 0 until MAIN_SIZE -> {
                val stack = main[slot]
                main[slot] = EMPTY_STACK
                return stack
            }
            in MAIN_SIZE until MAIN_SIZE + ARMOUR_SIZE -> {
                val stack = armour[slot - MAIN_SIZE]
                armour[slot - MAIN_SIZE] = EMPTY_STACK
                return stack
            }
            MAIN_SIZE + ARMOUR_SIZE -> {
                val stack = offHand
                offHand = EMPTY_STACK
                return stack
            }
            else -> return EMPTY_STACK
        }
    }

    override fun removeStack(slot: Int, amount: Int): ItemStack {
        TODO("Not yet implemented")
    }

    override fun isValidItem(slot: Int, item: ItemStack): Boolean {
        TODO("Not yet implemented")
    }

    override fun count(item: Item): Int {
        return main.filter { it.item == item }.sumOf { it.count } +
            armour.filter { it.item == item }.sumOf { it.count } +
            if (offHand.item == item) offHand.count else 0
    }

    override fun contains(item: Item): Boolean {
        return main.any { it.item == item } || armour.any { it.item == item } || offHand.item == item
    }

    override fun containsAny(vararg items: Item): Boolean {
        return main.any { items.contains(it.item) } || armour.any { items.contains(it.item) } || items.contains(offHand.item)
    }

    fun forEachIndexed(function: (Int, ItemStack) -> Unit) {
        for (i in 0 until size) {
            function(i, get(i))
        }
    }
}