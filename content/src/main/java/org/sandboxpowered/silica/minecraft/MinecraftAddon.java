package org.sandboxpowered.silica.minecraft;

import com.google.common.collect.Sets;
import org.sandboxpowered.api.SandboxAPI;
import org.sandboxpowered.api.addon.Addon;
import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Materials;
import org.sandboxpowered.api.block.SlabBlock;
import org.sandboxpowered.api.item.BaseItem;
import org.sandboxpowered.api.item.Item;
import org.sandboxpowered.api.registry.Registrar;
import org.sandboxpowered.silica.minecraft.blocks.*;
import org.sandboxpowered.silica.minecraft.fluid.EmptyFluid;

import java.util.Collections;
import java.util.Locale;
import java.util.Set;
import java.util.function.Function;

import static org.sandboxpowered.api.block.Block.Settings.builder;

public class MinecraftAddon implements Addon {
    @Override
    public void init(SandboxAPI api) {
        api.getLog().info("Loading Minecraft Content - 1.16.5");
    }

    @Override
    public void register(SandboxAPI api, Registrar registrar) {
        registerBlocks(registrar);
        registerItems(registrar);
        registerFluids(registrar);
    }

    public void registerBlocks(Registrar registrar) {
        Extra[] baseExtras = new Extra[]{Extra.SLAB, Extra.STAIRS};
        Extra[] extrasWithWall = new Extra[]{Extra.SLAB, Extra.STAIRS, Extra.WALL};

        registrar.register("air", new AirBlock(builder(Materials.AIR).removeItemBlock().build()));

        registerWithExtra(registrar, "stone", new BaseBlock(builder(Materials.STONE).build()), Extra.SLAB, Extra.STAIRS, Extra.BUTTON);
        registerWithExtra(registrar, "cobblestone", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registerWithExtra(registrar, "mossy_cobblestone", new BaseBlock(builder(Materials.STONE).build()), Extra.WALL);
        registerWithExtraPluraliseNormal(registrar, "stone_brick", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registerWithExtraPluraliseNormal(registrar, "mossy_stone_brick", new BaseBlock(builder(Materials.STONE).build()), Extra.WALL);

        registerWithExtra(registrar, "prismarine", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registerWithExtraPluraliseNormal(registrar, "brick", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);

        registerWithExtra(registrar, "granite", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registerWithExtra(registrar, "diorite", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registerWithExtra(registrar, "andesite", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registrar.register("dirt", new BaseBlock(builder(Materials.EARTH).build()));
        registrar.register("grass_block", new SpreadingBlock(builder(Materials.EARTH).ticksRandomly().build()));
        registrar.register("bedrock", new BaseBlock(builder(Materials.STONE).setStrength(-1).build()));
        registrar.register("sand", new BaseBlock(builder(Materials.SAND).build()));
        registrar.register("red_sand", new BaseBlock(builder(Materials.SAND).build()));

        registrar.register("netherrack", new BaseBlock(builder(Materials.STONE).build()));
        registrar.register("glowstone", new BaseBlock(builder(Materials.GLASS).setLuminance(15).build()));
        registerWithExtra(registrar, "blackstone", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registerWithExtra(registrar, "polished_blackstone", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registerWithExtraPluraliseNormal(registrar, "polished_blackstone_brick", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registerWithExtraPluraliseNormal(registrar, "nether_brick", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);
        registerWithExtraPluraliseNormal(registrar, "red_nether_brick", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);


        registerWithExtraPluraliseNormal(registrar, "end_stone_brick", new BaseBlock(builder(Materials.STONE).build()), extrasWithWall);

        for (Colour colour : Colour.values()) {
            registrar.register(String.format("%s_wool", colour.getName()), new BaseBlock(builder(Materials.WOOL).build()));
            registrar.register(String.format("%s_carpet", colour.getName()), new CarpetBlock(builder(Materials.WOOL).build()));

            registrar.register(String.format("%s_bed", colour.getName()), new BlockBed(builder(Materials.WOOD).build()));
            registrar.register(String.format("%s_banner", colour.getName()), new BannerBlock(builder(Materials.WOOD).build()));

            registrar.register(String.format("%s_concrete_powder", colour.getName()), new BaseBlock(builder(Materials.SAND).build()));

            registrar.register(String.format("%s_concrete", colour.getName()), new BaseBlock(builder(Materials.STONE).build()));
            registrar.register(String.format("%s_terracotta", colour.getName()), new BaseBlock(builder(Materials.STONE).build()));

            registrar.register(String.format("%s_stained_glass", colour.getName()), new BaseBlock(builder(Materials.GLASS).build()));
            registrar.register(String.format("%s_stained_glass_pane", colour.getName()), new GlassPaneBlock(builder(Materials.GLASS).build()));
        }

        for (Sandstone sandstone : Sandstone.values()) {
            Extra[] extras = switch (sandstone) {
                case CUT -> new Extra[]{Extra.SLAB};
                case CHISELED -> null;
                case NORMAL -> extrasWithWall;
                default -> baseExtras;
            };
            registerWithExtra(registrar, sandstone.formatted("sandstone"), new BaseBlock(builder(Materials.SAND).build()), extras);
            registerWithExtra(registrar, sandstone.formatted("red_sandstone"), new BaseBlock(builder(Materials.SAND).build()), extras);
        }

        for (Wood wood : Wood.values()) {
            registrar.register(String.format("%s_leaves", wood.getPrefix()), new LeavesBlock(builder(Materials.LEAVES).build()));
            registrar.register(String.format("%s_log", wood.getPrefix()), new AxisBlock(builder(Materials.WOOD).build()));
            registrar.register(String.format("stripped_%s_log", wood.getPrefix()), new AxisBlock(builder(Materials.WOOD).build()));

            registrar.register(String.format("%s_wood", wood.getPrefix()), new AxisBlock(builder(Materials.WOOD).build()));
            registrar.register(String.format("stripped_%s_wood", wood.getPrefix()), new AxisBlock(builder(Materials.WOOD).build()));

            BaseBlock plank = new BaseBlock(builder(Materials.WOOD).build());
            registrar.register(String.format("%s_planks", wood.getPrefix()), plank);

            registrar.register(String.format("%s_button", wood.getPrefix()), new ButtonBlock(builder(plank).build()));
            registrar.register(String.format("%s_sign", wood.getPrefix()), new SignBlock(builder(plank).build()));
            registrar.register(String.format("%s_wall_sign", wood.getPrefix()), new WallSignBlock(builder(plank).build()));
            registrar.register(String.format("%s_slab", wood.getPrefix()), new SlabBlock(builder(plank).build()));
            registrar.register(String.format("%s_stairs", wood.getPrefix()), new StairsBlock(builder(plank).build()));
        }

        registrar.register("note_block", new NoteBlock(builder(Materials.WOOD).build()));
        registrar.register("redstone_wire", new RedstoneWireBlock(builder(Materials.PART).build()));
    }

    public void registerItems(Registrar registrar) {
        registrar.register("air", new BaseItem(new Item.Settings()));
    }

    public void registerFluids(Registrar registrar) {
        registrar.register("empty", new EmptyFluid());
    }

    public void registerWithExtra(Registrar registrar, Function<Extra, String> nameFunction, BaseBlock block, Extra... extras) {
        Set<Extra> extraSet = extras == null ? Collections.emptySet() : Sets.newHashSet(extras);
        registrar.register(nameFunction.apply(null), block);
        if (extraSet.contains(Extra.SLAB))
            registrar.register(nameFunction.apply(Extra.SLAB), new SlabBlock(builder(block).build()));
        if (extraSet.contains(Extra.STAIRS))
            registrar.register(nameFunction.apply(Extra.STAIRS), new StairsBlock(builder(block).build()));
        if (extraSet.contains(Extra.WALL))
            registrar.register(nameFunction.apply(Extra.WALL), new WallBlock(builder(block).build()));
        if (extraSet.contains(Extra.BUTTON))
            registrar.register(nameFunction.apply(Extra.BUTTON), new ButtonBlock(builder(block).build()));
    }

    public void registerWithExtra(Registrar registrar, String name, BaseBlock block, Extra... extras) {
        registerWithExtra(registrar, e -> e == null ? name : e.format(name), block, extras);
    }

    public void registerWithExtraPluraliseNormal(Registrar registrar, String name, BaseBlock block, Extra... extras) {
        registerWithExtra(registrar, e -> e == null ? name+"s" : e.format(name), block, extras);
    }

    public enum Extra {
        BUTTON,
        SLAB,
        STAIRS,
        WALL;

        public String format(String name) {
            return String.format("%s_%s", name, name().toLowerCase(Locale.ENGLISH));
        }
    }
}