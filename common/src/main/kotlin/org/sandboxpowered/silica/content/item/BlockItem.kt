package org.sandboxpowered.silica.content.item

import org.sandboxpowered.silica.content.block.Block
import org.sandboxpowered.silica.util.Identifier

class BlockItem(identifier: Identifier, val block: Block, properties: Item.Properties) :
    BaseItem(identifier, properties) {
    override fun translationKey(): String = block.translationKey()
}