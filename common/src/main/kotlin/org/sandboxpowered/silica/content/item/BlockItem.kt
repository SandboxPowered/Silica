package org.sandboxpowered.silica.content.item

import org.sandboxpowered.silica.content.block.Block

class BlockItem(val block: Block, properties: Item.Properties) :
    BaseItem(block.identifier, properties) {
    override fun translationKey(): String = block.translationKey()
}