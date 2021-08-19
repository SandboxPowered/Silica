package org.sandboxpowered.silica.block

import org.sandboxpowered.silica.registry.RegistryObject
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.util.Identifier

object Blocks {
    val AIR: RegistryObject<Block> = SilicaRegistries.BLOCK_REGISTRY[Identifier.of("air")]
    val BEDROCK: RegistryObject<Block> = SilicaRegistries.BLOCK_REGISTRY[Identifier.of("bedrock")]
    val STONE: RegistryObject<Block> = SilicaRegistries.BLOCK_REGISTRY[Identifier.of("stone")]
    val DIRT: RegistryObject<Block> = SilicaRegistries.BLOCK_REGISTRY[Identifier.of("dirt")]
    val GRASS_BLOCK: RegistryObject<Block> = SilicaRegistries.BLOCK_REGISTRY[Identifier.of("grass_block")]
}