package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.Identifier

object Blocks {
    val AIR by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("air")].guarantee()
    val BEDROCK by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("bedrock")].guarantee()
    val STONE by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("stone")].guarantee()
    val DIRT by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("dirt")].guarantee()
    val GRASS_BLOCK by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("grass_block")].guarantee()
}