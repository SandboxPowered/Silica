package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.Identifier

object Blocks {
    val AIR by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("air")].guarentee()
    val BEDROCK by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("bedrock")].guarentee()
    val STONE by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("stone")].guarentee()
    val DIRT by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("dirt")].guarentee()
    val GRASS_BLOCK by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("grass_block")].guarentee()
}