package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.block.*
import org.sandboxpowered.silica.fluid.Fluid
import org.sandboxpowered.silica.item.Item
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.content.Colour
import org.sandboxpowered.silica.util.Identifier.Companion.of as id

object SilicaRegistries {
    val EMPTY = Archetype()
    val BLOCK = EMPTY block ::BaseBlock
    val STAIRS = EMPTY suffix "_stairs" block ::StairBlock
    val SLAB = EMPTY suffix "_slab" block ::SlabBlock
    val WALL = EMPTY suffix "_wall" block ::WallBlock
    val DOOR = EMPTY suffix "_door" block ::DoorBlock
    val TRAPDOOR = EMPTY suffix "_trapdoor" block ::TrapdoorBlock
    val BUTTON = EMPTY suffix "_button" block ::ButtonBlock
    val LEAVES = EMPTY suffix "_leaves" block ::LeavesBlock

    val BLOCK_S = BLOCK suffix "s"
    val BLOCK_ORE = BLOCK suffix "_ore"
    val BLOCK_DEEPSLATE_ORE = BLOCK_ORE prefix "deepslate_"

    @JvmField
    val BLOCK_REGISTRY: Registry<Block> = SilicaRegistry(id("minecraft", "block"), Block::class.java).apply {
        register(BaseBlock(id("air")))
        register(BaseBlock(id("bedrock")))
        register(BaseBlock(id("dirt")))
        register(GrassBlock(id("grass_block")))
        register(GrassBlock(id("mycelium")))
        register(GrassBlock(id("podzol")))

        register(BaseBlock(id("sand")))
        register(BaseBlock(id("gravel")))

        register(BaseBlock(id("glass")))

        register(NoteBlock(id("note_block")))
        register(FireBlock(id("fire")))
        register(GlassPaneBlock(id("glass_pane")))

        for (colour in Colour.NAMES) {
            register(BaseBlock(id("${colour}_wool")))
            register(BaseBlock(id("${colour}_carpet")))
            register(BaseBlock(id("${colour}_stained_glass")))
            register(GlassPaneBlock(id("${colour}_stained_glass_pane")))
            register(BedBlock(id("${colour}_bed")))
        }

        blocks {
            arrayOf("oak", "spruce", "birch", "jungle", "dark_oak", "acacia", "warped", "crimson") defines {
                archetypes(BLOCK suffix "_planks", STAIRS, SLAB, DOOR, TRAPDOOR, BUTTON, LEAVES not "warped" not "crimson")
            }
            "iron" defines { archetypes(DOOR, TRAPDOOR) }
            "stone" defines { archetypes(BUTTON) }
            arrayOf("iron", "coal", "gold", "diamond", "emerald", "lapis") defines {
                archetypes(BLOCK_ORE, BLOCK_DEEPSLATE_ORE)
            }
            arrayOf("cobblestone", "diorite", "granite", "andesite") defines {
                archetypes(BLOCK, WALL, STAIRS, SLAB)
            }
            arrayOf("stone", "polished_diorite", "polished_granite", "polished_andesite") defines {
                archetypes(BLOCK, STAIRS, SLAB)
            }
            arrayOf(
                "exposed_cut_copper",
                "waxed_exposed_cut_copper",
                "cut_copper",
                "waxed_cut_copper",
                "waxed_oxidized_cut_copper",
                "waxed_weathered_cut_copper",
                "weathered_cut_copper",
                "oxidized_cut_copper"
            ) defines { archetypes(BLOCK, STAIRS, SLAB) }
            arrayOf(
                "exposed_copper",
                "waxed_exposed_copper",
                "copper_block",
                "waxed_copper_block",
                "waxed_oxidized_copper",
                "waxed_weathered_copper",
                "weathered_copper",
                "oxidized_copper"
            ) defines { archetypes(BLOCK) }
            "brick" defines { archetypes(BLOCK_S, WALL, STAIRS, SLAB) }
            "sandstone" defines { archetypes(BLOCK, WALL, STAIRS, SLAB) }
            "stone_brick" defines { archetypes(BLOCK_S, WALL, STAIRS, SLAB) }
            "mossy_stone_brick" defines { archetypes(BLOCK_S, WALL, STAIRS, SLAB) }
            "prismarine" defines { archetypes(BLOCK, WALL, STAIRS, SLAB) }
            "dark_prismarine" defines { archetypes(BLOCK, STAIRS, SLAB) }
            "nether_brick" defines { archetypes(BLOCK_S, WALL, STAIRS, SLAB) }
            "red_sandstone" defines { archetypes(BLOCK, WALL, STAIRS, SLAB) }
            "end_stone_brick" defines { archetypes(BLOCK_S, WALL, STAIRS, SLAB) }
            "polished_blackstone_brick" defines { archetypes(BLOCK_S, WALL, STAIRS, SLAB) }
            "blackstone" defines { archetypes(BLOCK, WALL, STAIRS, SLAB) }
            "mossy_cobblestone" defines { archetypes(BLOCK, WALL, STAIRS, SLAB) }
            "deepslate_brick" defines { archetypes(BLOCK_S, WALL, STAIRS, SLAB) }
            "deepslate_tile" defines { archetypes(BLOCK_S, WALL, STAIRS, SLAB) }
            "polished_deepslate" defines { archetypes(BLOCK, WALL, STAIRS, SLAB) }
            "red_nether_brick" defines { archetypes(BLOCK_S, WALL, STAIRS, SLAB) }
            "sandstone" defines { archetypes(BLOCK, WALL, STAIRS, SLAB) }
            "cobbled_deepslate" defines { archetypes(BLOCK, WALL, STAIRS, SLAB) }
        }
    }

    @JvmField
    val ITEM_REGISTRY: Registry<Item> = SilicaRegistry(id("minecraft", "item"), Item::class.java)

    @JvmField
    val FLUID_REGISTRY: Registry<Fluid> = SilicaRegistry(id("minecraft", "fluid"), Fluid::class.java)

    inline fun SilicaRegistry<Block>.blocks(block: Blocks.() -> Unit) {
        val blocks = Blocks(this)
        block(blocks)
    }

    class Blocks(val registry: SilicaRegistry<Block>) {
        private fun String.template(replacement: String): String =
            if ("{}" !in this) this else this.replace("{}", replacement)

        inline infix fun Array<String>.defines(block: GenericBlockDefinition.() -> Unit) {
            val generic = GenericBlockDefinition()
            block(generic)
            forEach { arr ->
                registerGeneric(arr, generic)
            }
        }

        fun registerGeneric(name: String, generic: GenericBlockDefinition) {
            generic.archetypes.forEach {
                val provider = it.blockProvider
                if (it.notOn?.contains(name) != true && provider != null) {
                    registry.register(provider(id(it.replacementString?.template(name) ?: name)))
                }
            }
        }

        inline infix fun String.defines(block: GenericBlockDefinition.() -> Unit) {
            val generic = GenericBlockDefinition()
            block(generic)
            registerGeneric(this, generic)
        }
    }

    class GenericBlockDefinition {
        val archetypes = ArrayList<Archetype>()

        fun archetypes(vararg types: Archetype) {
            types.forEach {
                archetypes.add(it)
            }
        }
    }

    class Archetype(
        val replacementString: String? = null,
        val notOn: Set<String>? = null,
        val blockProvider: ((Identifier) -> Block)? = null
    ) {

        infix fun with(replacement: String): Archetype {
            return Archetype(replacement, notOn, blockProvider)
        }

        infix fun not(other: String): Archetype {
            if (notOn != null)
                return Archetype(replacementString, notOn + other, blockProvider)
            return Archetype(replacementString, setOf(other), blockProvider)
        }

        infix fun suffix(s: String): Archetype {
            return Archetype("${replacementString ?: "{}"}$s", notOn, blockProvider)
        }

        infix fun prefix(s: String): Archetype {
            return Archetype("$s${replacementString ?: "{}"}", notOn, blockProvider)
        }

        infix fun block(provider: (Identifier) -> Block): Archetype {
            return Archetype(replacementString, notOn, provider)
        }
    }
}