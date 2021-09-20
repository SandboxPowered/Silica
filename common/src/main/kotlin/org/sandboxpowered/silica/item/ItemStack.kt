package org.sandboxpowered.silica.item

class ItemStack(val item: Item, var count: Int) {

    val isEmpty: Boolean
        get() {
            return count <= 0 || item.identifier.path == "air"
        }

}