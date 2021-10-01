package org.sandboxpowered.silica.content.item

import org.sandboxpowered.silica.registry.Registry
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.Identifier

open class BaseItem(override val identifier: Identifier, override val properties: Item.Properties) : Item {
    override val registry: Registry<Item>
        get() = SilicaRegistries.ITEM_REGISTRY
}