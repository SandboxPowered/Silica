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
import java.util.Set;

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

        registrar.register("air", new AirBlock(builder(Materials.AIR).removeItemBlock().build()));

        registerWithExtra(registrar, "stone", new BaseBlock(builder(Materials.STONE).build()), baseExtras);
        registerWithExtra(registrar, "cobblestone", new BaseBlock(builder(Materials.STONE).build()), baseExtras);
        registerWithExtra(registrar, "granite", new BaseBlock(builder(Materials.STONE).build()), baseExtras);
        registerWithExtra(registrar, "diorite", new BaseBlock(builder(Materials.STONE).build()), baseExtras);
        registerWithExtra(registrar, "andesite", new BaseBlock(builder(Materials.STONE).build()), baseExtras);
        registrar.register("dirt", new BaseBlock(builder(Materials.EARTH).build()));
        registrar.register("grass_block", new SpreadingBlock(builder(Materials.EARTH).ticksRandomly().build()));
        registrar.register("bedrock", new BaseBlock(builder(Materials.STONE).setStrength(-1).build()));
        registrar.register("sand", new BaseBlock(builder(Materials.SAND).build()));
        registrar.register("red_sand", new BaseBlock(builder(Materials.SAND).build()));

        registrar.register("netherrack", new BaseBlock(builder(Materials.STONE).build()));
        registrar.register("glowstone", new BaseBlock(builder(Materials.GLASS).setLuminance(15).build()));

        for (Colour colour : Colour.values()) {
            registrar.register(String.format("%s_wool", colour.getName()), new BaseBlock(builder(Materials.WOOL).build()));
            registrar.register(String.format("%s_carpet", colour.getName()), new CarpetBlock(builder(Materials.WOOL).build()));

            registrar.register(String.format("%s_concrete", colour.getName()), new BaseBlock(builder(Materials.STONE).build()));
            registrar.register(String.format("%s_concrete_powder", colour.getName()), new BaseBlock(builder(Materials.SAND).build()));
            registrar.register(String.format("%s_terracotta", colour.getName()), new BaseBlock(builder(Materials.STONE).build()));
            registrar.register(String.format("%s_stained_glass", colour.getName()), new BaseBlock(builder(Materials.GLASS).build()));
            registrar.register(String.format("%s_stained_glass_pane", colour.getName()), new GlassPaneBlock(builder(Materials.GLASS).build()));
        }

        for (Sandstone sandstone : Sandstone.values()) {
            Extra[] extras = switch (sandstone) {
                case CUT -> new Extra[]{Extra.SLAB};
                case CHISELED -> null;
                default -> baseExtras;
            };
            registerWithExtra(registrar, sandstone.formatted("sandstone"), new BaseBlock(builder(Materials.SAND).build()), extras);
            registerWithExtra(registrar, sandstone.formatted("red_sandstone"), new BaseBlock(builder(Materials.SAND).build()), extras);
        }

        for (Wood wood : Wood.values()) {
            registrar.register(String.format("%s_log", wood.getPrefix()), new AxisBlock(builder(Materials.WOOD).build()));
            registrar.register(String.format("stripped_%s_log", wood.getPrefix()), new AxisBlock(builder(Materials.WOOD).build()));

            registrar.register(String.format("%s_wood", wood.getPrefix()), new AxisBlock(builder(Materials.WOOD).build()));
            registrar.register(String.format("stripped_%s_wood", wood.getPrefix()), new AxisBlock(builder(Materials.WOOD).build()));

            BaseBlock plank = new BaseBlock(builder(Materials.WOOD).build());
            registerWithExtra(registrar, String.format("%s_planks", wood.getPrefix()), plank);
            registerWithExtra(registrar, String.format("%s_slab", wood.getPrefix()), new SlabBlock(builder(plank).build()));
            registerWithExtra(registrar, String.format("%s_stairs", wood.getPrefix()), new StairsBlock(builder(plank).build()));
        }
    }

    public void registerItems(Registrar registrar) {
        registrar.register("air", new BaseItem(new Item.Settings()));
    }

    public void registerFluids(Registrar registrar) {
        registrar.register("empty", new EmptyFluid());
    }

    public void registerWithExtra(Registrar registrar, String name, BaseBlock block, Extra... extras) {
        Set<Extra> extraSet = extras == null ? Collections.emptySet() : Sets.newHashSet(extras);
        registrar.register(name, block);
        if (extraSet.contains(Extra.SLAB))
            registrar.register(String.format("%s_slab", name), new SlabBlock(builder(block).build()));
        if (extraSet.contains(Extra.STAIRS))
            registrar.register(String.format("%s_stairs", name), new StairsBlock(builder(block).build()));
    }

    public enum Extra {
        SLAB,
        STAIRS
    }
}