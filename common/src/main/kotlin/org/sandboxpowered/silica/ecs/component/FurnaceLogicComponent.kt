package org.sandboxpowered.silica.ecs.component

import com.artemis.PooledComponent
import org.sandboxpowered.silica.recipe.SmeltingRecipe

class FurnaceLogicComponent : PooledComponent() {
    var fuelTime: Int = 0
    var fuelTimeTotal: Int = 0

    var smeltingTime: Int = 0
    var smeltingTimeTotal: Int = 0

    var cachedRecipe: SmeltingRecipe? = null

    var new: Boolean = true

    override fun reset() {
        fuelTime = 0
        fuelTimeTotal = 0
        smeltingTime = 0
        smeltingTimeTotal = 0
        cachedRecipe = null
        new = true
    }
}