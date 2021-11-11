package org.sandboxpowered.silica.vanilla

import org.sandboxpowered.silica.api.block.BaseBlock
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.item.BaseItem
import org.sandboxpowered.silica.api.item.BlockItem
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.vanilla.block.*
import org.sandboxpowered.silica.vanilla.util.Colour

object SilicaInit {
    private val EMPTY = BlockArchetype()
    private val BLOCK = EMPTY block ::BaseBlock
    private val STAIRS = EMPTY suffix "_stairs" block ::StairBlock
    private val SLAB = EMPTY suffix "_slab" block ::SlabBlock
    private val WALL = EMPTY suffix "_wall" block ::WallBlock
    private val FENCE = EMPTY suffix "_fence" block ::FenceBlock
    private val FENCE_GATE = EMPTY suffix "_fence_gate" block ::FenceGateBlock
    private val DOOR = EMPTY suffix "_door" block ::DoorBlock
    private val TRAPDOOR = EMPTY suffix "_trapdoor" block ::TrapdoorBlock
    private val BUTTON = EMPTY suffix "_button" block ::ButtonBlock
    private val LEAVES = EMPTY suffix "_leaves" block ::LeavesBlock
    private val GRASS = EMPTY block ::GrassBlock

    private val SIGN = EMPTY suffix "_sign" block ::SignBlock
    private val WALL_SIGN = EMPTY suffix "_wall_sign" block ::WallSignBlock

    private val BLOCK_S = BLOCK suffix "s"
    private val BLOCK_ORE = BLOCK suffix "_ore"
    private val BLOCK_DEEPSLATE_ORE = BLOCK_ORE prefix "deepslate_"

    fun init() {
        register(BaseItem(Identifier("air")))
        register(BaseItem(Identifier("iron_ingot")))
        register(BaseItem(Identifier("coal"), Item.Properties.create {
            fuelTime = 1600
        }))

        register(BaseBlock(Identifier("air")), false)
        register(BaseBlock(Identifier("bedrock")))
        register(BaseBlock(Identifier("dirt")))

        register(BaseBlock(Identifier("glowstone")))
        register(GlowLichenBlock(Identifier("glow_lichen")))

        register(BaseBlock(Identifier("sand")))
        register(BaseBlock(Identifier("gravel")))

        register(BaseBlock(Identifier("glass")))

        register(NoteBlock(Identifier("note_block")))

        register(TripwireBlock(Identifier("tripwire")))
        register(TripwireHookBlock(Identifier("tripwire_hook")))

        register(FireBlock(Identifier("fire")))
        register(GlassPaneBlock(Identifier("glass_pane")))

        register(FurnaceBlock(Identifier("furnace")))
        register(RedstoneWireBlock(Identifier("redstone_wire")))

        register(BeehiveBlock(Identifier("beehive")))
        register(BeehiveBlock(Identifier("bee_nest")))

        blocks {
            Colour.NAME_ARRAY defines {
                archetypes(BLOCK suffix "_wool")
                archetypes(BLOCK suffix "_carpet")
                archetypes(BLOCK suffix "_stained_glass")
                archetypes(EMPTY suffix "_stained_glass_pane" block ::GlassPaneBlock)
                archetypes(EMPTY suffix "_bed" block ::BedBlock)
                archetypes(EMPTY suffix "_banner" block ::BannerBlock)
                archetypes(EMPTY suffix "_candle" block ::CandleBlock)
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
                    FENCE,
                    FENCE_GATE,
                    SIGN,
                    WALL_SIGN,
                    LEAVES not "warped" not "crimson"
                )
            }
            arrayOf("nether_wart_block", "warped_wart_block") defines {
                archetypes(BLOCK)
            }
            "iron" defines { archetypes(DOOR, TRAPDOOR) }
            "stone" defines { archetypes(BUTTON) }
            arrayOf("iron", "coal", "gold", "diamond", "emerald", "lapis") defines {
                archetypes(BLOCK_ORE, BLOCK_DEEPSLATE_ORE, BLOCK suffix "_block")
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
            "polished_blackstone" defines { archetypes(BLOCK, WALL, STAIRS, SLAB, BUTTON) }
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

    private fun register(block: Block, registerItemBlock: Boolean = true) {
        Registries.BLOCKS.register(block)
        if (block.hasItem && registerItemBlock) {
            Registries.ITEMS.register(BlockItem(block))
        }
    }

    private fun register(item: Item) {
        Registries.ITEMS.register(item)
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
                    register(provider(Identifier(it.replacementString?.template(name) ?: name)))
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