package org.sandboxpowered.silica.registry

import org.sandboxpowered.silica.block.*
import org.sandboxpowered.silica.item.BaseItem
import org.sandboxpowered.silica.item.BlockItem
import org.sandboxpowered.silica.util.Identifier
import org.sandboxpowered.silica.util.content.Colour
import org.sandboxpowered.silica.util.Identifier.Companion.of as id

object SilicaInit {
    private val EMPTY = BlockArchetype()
    private val BLOCK = EMPTY block ::BaseBlock
    private val STAIRS = EMPTY suffix "_stairs" block ::StairBlock
    private val SLAB = EMPTY suffix "_slab" block ::SlabBlock
    private val WALL = EMPTY suffix "_wall" block ::WallBlock
    private val DOOR = EMPTY suffix "_door" block ::DoorBlock
    private val TRAPDOOR = EMPTY suffix "_trapdoor" block ::TrapdoorBlock
    private val BUTTON = EMPTY suffix "_button" block ::ButtonBlock
    private val LEAVES = EMPTY suffix "_leaves" block ::LeavesBlock
    private val GRASS = EMPTY block ::GrassBlock

    private val BLOCK_S = BLOCK suffix "s"
    private val BLOCK_ORE = BLOCK suffix "_ore"
    private val BLOCK_DEEPSLATE_ORE = BLOCK_ORE prefix "deepslate_"

    fun init() {
        SilicaRegistries.ITEM_REGISTRY.register(BaseItem(id("air")))

        register(BaseBlock(id("air")))
        register(BaseBlock(id("bedrock")))
        register(BaseBlock(id("dirt")))

        register(BaseBlock(id("sand")))
        register(BaseBlock(id("gravel")))

        register(BaseBlock(id("glass")))

        register(NoteBlock(id("note_block")))
        register(FireBlock(id("fire")))
        register(GlassPaneBlock(id("glass_pane")))

        blocks {
            Colour.NAME_ARRAY defines {
                archetypes(BLOCK suffix "_wool")
                archetypes(BLOCK suffix "_carpet")
                archetypes(BLOCK suffix "_stained_glass")
                archetypes(EMPTY suffix "_stained_glass_pane" block ::GlassPaneBlock)
                archetypes(EMPTY suffix "_bed" block ::BedBlock)
            }
            arrayOf("grass_block", "mycelium", "podzol") defines {
                archetypes(GRASS)
            }
            arrayOf("oak", "spruce", "birch", "jungle", "dark_oak", "acacia", "warped", "crimson") defines {
                archetypes(
                    BLOCK suffix "_planks",
                    STAIRS,
                    SLAB,
                    DOOR,
                    TRAPDOOR,
                    BUTTON,
                    LEAVES not "warped" not "crimson"
                )
            }
            arrayOf("nether_wart_block", "warped_wart_block") defines {
                archetypes(BLOCK)
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

    private fun register(block: Block) {
        SilicaRegistries.BLOCK_REGISTRY.register(block)
        if (!block.isAir) {
            SilicaRegistries.ITEM_REGISTRY.register(BlockItem(block.identifier, block))
        }
    }

    inline fun blocks(block: Blocks.() -> Unit) {
        val blocks = Blocks()
        block(blocks)
    }

    class Blocks {
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
                    register(provider(id(it.replacementString?.template(name) ?: name)))
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
        val archetypes = ArrayList<BlockArchetype>()

        fun archetypes(vararg types: BlockArchetype) {
            types.forEach {
                archetypes.add(it)
            }
        }
    }

    class BlockArchetype(
        val replacementString: String? = null,
        val notOn: Set<String>? = null,
        val blockProvider: ((Identifier) -> Block)? = null
    ) {

        infix fun with(replacement: String): BlockArchetype {
            return BlockArchetype(replacement, notOn, blockProvider)
        }

        infix fun not(other: String): BlockArchetype {
            if (notOn != null)
                return BlockArchetype(replacementString, notOn + other, blockProvider)
            return BlockArchetype(replacementString, setOf(other), blockProvider)
        }

        infix fun suffix(s: String): BlockArchetype {
            return BlockArchetype("${replacementString ?: "{}"}$s", notOn, blockProvider)
        }

        infix fun prefix(s: String): BlockArchetype {
            return BlockArchetype("$s${replacementString ?: "{}"}", notOn, blockProvider)
        }

        infix fun block(provider: (Identifier) -> Block): BlockArchetype {
            return BlockArchetype(replacementString, notOn, provider)
        }
    }
}