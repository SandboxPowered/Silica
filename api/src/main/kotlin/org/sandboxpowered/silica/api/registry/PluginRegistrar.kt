package org.sandboxpowered.silica.api.registry

import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.fluid.Fluid
import org.sandboxpowered.silica.api.item.Item

interface PluginRegistrar {
    val blocks: ContentRegistrar<Block>
    val items: ContentRegistrar<Item>
    val fluids: ContentRegistrar<Fluid>
}