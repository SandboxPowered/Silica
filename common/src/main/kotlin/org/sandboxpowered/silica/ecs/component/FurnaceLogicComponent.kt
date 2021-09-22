package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent

class FurnaceLogicComponent : PooledComponent() {
    private var fuelStart: Long = 0
    private var smeltingStart: Long = 0

    override fun reset() {
        fuelStart = 0
        smeltingStart = 0
    }
}