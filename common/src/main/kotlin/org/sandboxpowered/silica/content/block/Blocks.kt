package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.registry.Registry
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.registry.SilicaRegistries.blocks
import org.sandboxpowered.silica.util.Identifier

object Blocks {
    val AIR by blocks().guaranteed
    val BEDROCK by blocks().guaranteed
    val STONE by blocks().guaranteed
    val DIRT by blocks().guaranteed
    val IRON_FURNACE by blocks("ic2")
    val GRASS_BLOCK by blocks().guaranteed
}