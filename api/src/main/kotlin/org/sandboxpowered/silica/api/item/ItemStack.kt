package org.sandboxpowered.silica.api.item

import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.RegistryObject
import kotlin.math.min

class ItemStack private constructor(private var _item: Item, private var _count: Int) {
    companion object {
        private val AIR by Registries.items()
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
        get() = _count <= 0 || _item.identifier.path == "air"
    val isNotEmpty: Boolean
        get() = !isEmpty

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

    fun isItemEqual(item: Item): Boolean = item == this.item

    fun canMerge(stack: ItemStack): Boolean = this.isEmpty || isItemEqual(stack.item) // TODO: match NBT when we have it

    override fun toString(): String = "ItemStack(item=\"${item.identifier}\", count=$count)"

    /**
     * Merge this with given [stack] and return the remainder
     */
    fun merge(stack: ItemStack): ItemStack {
        if (!canMerge(stack)) return stack

        val max = this.item.properties.maxStackSize
        val toAdd = min(max - count, stack.count)
        this += toAdd
        this._item = stack.item
        return stack.duplicate().apply {
            this -= toAdd
        }
    }
}