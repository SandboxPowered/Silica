package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent
import org.sandboxpowered.silica.content.inventory.BaseInventory

class ResizableInventoryComponent : PooledComponent() {
    val inventory = BaseInventory().apply {
        section(3) // TODO instead rely on a resizable inventory
    }

    override fun reset() = inventory.clear()
}