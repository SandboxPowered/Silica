package org.sandboxpowered.silica.content.item

import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.api.util.Identifier

open class BaseItem(
    override val identifier: Identifier,
    override val properties: Item.Properties = Item.Properties.create {}
) : Item {
    override val registry: Registry<Item>
        get() = SilicaRegistries.ITEM_REGISTRY
}