package org.sandboxpowered.silica.api.item

import org.sandboxpowered.utilities.Identifier

open class BaseItem(
    override val identifier: Identifier,
    override val properties: Item.Properties = Item.Properties.create {}
) : Item