package org.sandboxpowered.silica.content.item

import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.Identifier.Companion.of as id

class ItemStack(private val _item: Item, private var _count: Int) {
    companion object {
        private val AIR = SilicaRegistries.ITEM_REGISTRY[id("air")].get()
        val EMPTY = ItemStack(AIR, 0)

        fun of(item: Item): ItemStack {
            return of(item, 1)
        }

        fun of(item: Item, count: Int): ItemStack {
            return ItemStack(item, count)
        }
    }

    val item: Item
        get() = if (isEmpty) AIR else _item

    val isEmpty: Boolean
        get() {
            return _count <= 0 || _item.identifier.path == "air"
        }

    var count: Int
        get() = _count
        set(value) {
            _count = value
        }

    operator fun plusAssign(i: Int) {
        _count += i
    }

    operator fun minusAssign(i: Int) {
        _count -= i
    }
}