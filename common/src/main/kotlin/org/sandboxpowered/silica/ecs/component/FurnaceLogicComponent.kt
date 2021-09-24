package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent

class FurnaceLogicComponent : PooledComponent() {
    var fuelTime: Float = 0f
    var smeltingTime: Float = 0f

    override fun reset() {
        fuelTime = 0f
        smeltingTime = 0f
    }
}