package org.sandboxpowered.silica.content.block

import org.sandboxpowered.silica.registry.RegistryObject
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.Identifier

object Blocks {
    val AIR by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("air")].nonnull()
    val BEDROCK by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("bedrock")].nonnull()
    val STONE by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("stone")].nonnull()
    val DIRT by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("dirt")].nonnull()
    val GRASS_BLOCK by SilicaRegistries.BLOCK_REGISTRY[Identifier.of("grass_block")].nonnull()
}