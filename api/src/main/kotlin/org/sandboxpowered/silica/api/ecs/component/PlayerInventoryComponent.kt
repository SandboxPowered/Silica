package org.sandboxpowered.silica.api.ecs.component

import com.artemis.PooledComponent
import org.sandboxpowered.silica.api.item.inventory.PlayerInventory

class PlayerInventoryComponent : PooledComponent() {
    val inventory = PlayerInventory()
    override fun reset() {
        inventory.clear()
    }
}