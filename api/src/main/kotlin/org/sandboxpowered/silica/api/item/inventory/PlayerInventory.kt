package org.sandboxpowered.silica.api.item.inventory

import org.sandboxpowered.silica.api.item.ItemStack

class PlayerInventory : BaseInventory() {
    companion object {
        const val CRAFTING_SIZE = 5
        const val ARMOUR_SIZE = 4
        const val MAIN_SIZE = 27
        const val HOTBAR_SIZE = 9
        const val OFFHAND_SIZE = 1
    }

    val crafting = section(CRAFTING_SIZE)
    val armour = section(ARMOUR_SIZE)
    val main = section(MAIN_SIZE)
    val hotbar = section(HOTBAR_SIZE)
    var offHand = section(OFFHAND_SIZE)

    var selectedSlot: Int = 0
        set(value) {
            if (isValidHotbarIndex(value)) field = value
        }

    val mainHandStack: ItemStack
        get() = if (isValidHotbarIndex(selectedSlot)) hotbar[selectedSlot] else ItemStack.EMPTY

    private fun isValidHotbarIndex(slot: Int): Boolean = slot in 0 until HOTBAR_SIZE

}