package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.registry.SilicaRegistries.blocks

object Blocks {
    val AIR by blocks()

    //TODO: Dont require these blocks to be registered
    val BEDROCK by blocks()
    val STONE by blocks()
    val DIRT by blocks()
    val GRASS_BLOCK by blocks()
}