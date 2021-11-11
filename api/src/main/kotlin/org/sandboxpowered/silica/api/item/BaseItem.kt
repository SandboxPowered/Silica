package org.sandboxpowered.silica.api.item

import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.util.Identifier

open class BaseItem(
    override val identifier: Identifier,
    override val properties: Item.Properties = Item.Properties.create {}
) : Item {
    override val registry: Registry<Item>
        get() = Registries.ITEMS
}