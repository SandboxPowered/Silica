package org.sandboxpowered.silica.vanilla.ecs

import com.artemis.PooledComponent
import org.sandboxpowered.silica.api.item.inventory.BaseInventory

class ResizableInventoryComponent : PooledComponent() {
    val inventory = BaseInventory().apply {
        section(3) // TODO instead rely on a resizable inventory
    }

    override fun reset() = inventory.clear()
}