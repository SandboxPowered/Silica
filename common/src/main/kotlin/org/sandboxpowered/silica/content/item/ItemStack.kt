package org.sandboxpowered.silica.content.item

import org.sandboxpowered.silica.content.block.Block
import org.sandboxpowered.silica.registry.RegistryObject
import org.sandboxpowered.silica.registry.SilicaRegistries.items

class ItemStack private constructor(private val _item: Item, private var _count: Int) {
    companion object {
        private val AIR by items().guaranteed

        val EMPTY by lazy { ItemStack(AIR, 0) }

        operator fun invoke(block: Block): ItemStack = invoke(block.item, 1)

        operator fun invoke(obj: RegistryObject<Item>): ItemStack = invoke(obj, 1)

        operator fun invoke(obj: RegistryObject<Item>, count: Int): ItemStack = invoke(obj.orNull(), count)

        operator fun invoke(item: Item?): ItemStack = invoke(item, 1)

        operator fun invoke(item: Item?, count: Int): ItemStack = ItemStack(item ?: AIR, count)
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
        return ItemStack(_item, _count)
    }

    fun isItemEqual(obj: RegistryObject<Item>): Boolean =
        if (obj.isEmpty) false else item == obj.get()

    fun isItemEqual(item: Item): Boolean = this.item == item
}