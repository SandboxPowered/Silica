package org.sandboxpowered.silica.content.item

import org.sandboxpowered.silica.content.block.Block
import org.sandboxpowered.silica.registry.RegistryObject
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.Identifier.Companion.of as id

class ItemStack private constructor(private val _item: Item, private var _count: Int) {
    companion object {
        private val AIR by SilicaRegistries.ITEM_REGISTRY[id("air")].guarentee()

        val EMPTY by lazy { of(AIR, 0) }

        fun of(block: Block): ItemStack = of(block.item, 1)

        fun of(obj: RegistryObject<Item>): ItemStack = of(obj, 1)

        fun of(obj: RegistryObject<Item>, count: Int): ItemStack = of(obj.orNull(), count)

        fun of(item: Item?): ItemStack = of(item, 1)

        fun of(item: Item?, count: Int): ItemStack = ItemStack(item ?: AIR, count)
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

    fun duplicate(): ItemStack {
        return of(item, count)
    }

    fun isItemEqual(obj: RegistryObject<Item>): Boolean =
        if (obj.isEmpty) false else item == obj.get()

    fun isItemEqual(item: Item): Boolean = this.item == item
}