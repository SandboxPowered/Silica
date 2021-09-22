package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent
import org.sandboxpowered.silica.content.inventory.PlayerInventory

class PlayerInventoryComponent(val inventory: PlayerInventory) : PooledComponent() {
    override fun reset() {
        inventory.reset()
    }
}