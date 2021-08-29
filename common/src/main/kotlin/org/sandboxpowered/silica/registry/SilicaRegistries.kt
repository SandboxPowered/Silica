package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.block.*
import org.sandboxpowered.silica.fluid.Fluid
import org.sandboxpowered.silica.item.Item
import org.sandboxpowered.silica.util.content.Colour
import org.sandboxpowered.silica.util.content.Stone
import org.sandboxpowered.silica.util.Identifier.Companion.of as id

object SilicaRegistries {
    @JvmField
    val BLOCK_REGISTRY: Registry<Block> = SilicaRegistry(id("minecraft", "block"), Block::class.java).apply {
        register(BaseBlock(id("air")))
        register(BaseBlock(id("bedrock")))
        register(BaseBlock(id("dirt")))
        register(GrassBlock(id("grass_block")))

        register(BaseBlock(id("sand")))
        register(BaseBlock(id("gravel")))

        register(BaseBlock(id("glass")))

        for (colour in Colour.NAMES) {
            register(BaseBlock(id("${colour}_wool")))
            register(BaseBlock(id("${colour}_carpet")))
            register(BaseBlock(id("${colour}_stained_glass")))
        }
        for (stone in Stone.NAMES) {
            register(BaseBlock(id(stone)))
            register(SlabBlock(id("${stone}_slab")))
            register(StairBlock(id("${stone}_stairs")))
            if (stone != Stone.STONE.getName())
                register(WallBlock(id("${stone}_wall")))
        }
        for (wood in arrayOf("oak", "spruce", "birch", "jungle", "dark_oak", "acacia")) {
            register(BaseBlock(id("${wood}_planks")))
            register(SlabBlock(id("${wood}_slab")))
            register(StairBlock(id("${wood}_stairs")))
        }
    }

    @JvmField
    val ITEM_REGISTRY: Registry<Item> = SilicaRegistry(id("minecraft", "item"), Item::class.java)

    @JvmField
    val FLUID_REGISTRY: Registry<Fluid> = SilicaRegistry(id("minecraft", "fluid"), Fluid::class.java)
}