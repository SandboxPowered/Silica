package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent
import org.sandboxpowered.silica.content.inventory.PlayerInventory

class PlayerInventoryComponent : PooledComponent() {
    val inventory = PlayerInventory()
    override fun reset() {
        inventory.reset()
    }
}