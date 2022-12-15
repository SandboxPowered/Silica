package org.sandboxpowered.silica.vanilla.data

import org.sandboxpowered.silica.api.item.BaseItem
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.Identifier

object Items {

    fun init() {
        register(BaseItem(Identifier("air")))
        register(BaseItem(Identifier("iron_ingot")))
        register(BaseItem(Identifier("coal"), Item.Properties.create {
            fuelTime = 1600
        }))
    }

    private fun register(item: Item) {
        Registries.ITEMS.register(item)
    }
}