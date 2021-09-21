package org.sandboxpowered.silica.item

import net.mostlyoriginal.api.utils.pooling.ObjectPool
import net.mostlyoriginal.api.utils.pooling.Poolable
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.extensions.getPool
import org.sandboxpowered.silica.util.Identifier.Companion.of as id

class ItemStack private constructor(private var _item: Item, private var _count: Int) : Poolable {
    companion object {
        private val AIR = SilicaRegistries.ITEM_REGISTRY[id("air")].get()
        val EMPTY = ItemStack(SilicaRegistries.ITEM_REGISTRY[id("air")].get(), 0)
        val pool: ObjectPool<ItemStack> = getPool()

        fun of(item: Item): ItemStack {
            return of(item, 1)
        }

        fun of(item: Item, count: Int): ItemStack {
            val stack = pool.obtain()
            stack.internalSet(item, count)
            return stack
        }
    }

    @Suppress("unused")
    internal constructor() : this(AIR, 0)

    override fun reset() {
        _item = AIR
        _count = 0
    }

    private fun internalSet(item: Item, count: Int) {
        this._item = item
        this._count = count
    }

    val isEmpty: Boolean
        get() {
            return _count <= 0 || _item.identifier.path == "air"
        }

    val item: Item
        get() = _item
    var count: Int
        get() = _count
        set(value) {
            _count = value
        }

    fun free() {
        pool.free(this)
    }

    operator fun plusAssign(i: Int) {
        _count += i
    }

    operator fun minusAssign(i: Int) {
        _count -= i
    }
}