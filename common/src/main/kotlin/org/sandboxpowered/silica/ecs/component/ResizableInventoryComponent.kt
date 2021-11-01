package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent
import org.sandboxpowered.silica.content.inventory.ResizableInventory

class ResizableInventoryComponent : PooledComponent() {
    val inventory: ResizableInventory = ResizableInventory()

    override fun reset() = inventory.clear()
}