package org.sandboxpowered.silica.content.item

import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.Identifier.Companion.of as id

class ItemStack(val item: Item, private var _count: Int) {
    companion object {
        val EMPTY = ItemStack(SilicaRegistries.ITEM_REGISTRY[id("air")].get(), 0)

        fun of(item: Item): ItemStack {
            return of(item, 1)
        }

        fun of(item: Item, count: Int): ItemStack {
            return ItemStack(item, count)
        }
    }

    val isEmpty: Boolean
        get() {
            return _count <= 0 || item.identifier.path == "air"
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