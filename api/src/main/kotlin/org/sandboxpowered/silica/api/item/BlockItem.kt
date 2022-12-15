package org.sandboxpowered.silica.api.item

import org.sandboxpowered.silica.api.block.Block

class BlockItem(
    val block: Block,
    properties: Item.Properties = Item.Properties.create {}
) : BaseItem(block.identifier, properties) {
    override fun translationKey(): String = block.translationKey()
}