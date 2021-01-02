package org.sandboxpowered.silica.content;

import com.google.common.collect.Sets;
import org.sandboxpowered.api.SandboxAPI;
import org.sandboxpowered.api.addon.Addon;
import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Material;
import org.sandboxpowered.api.block.SlabBlock;
import org.sandboxpowered.api.registry.Registrar;
import org.sandboxpowered.silica.content.blocks.AirBlock;
import org.sandboxpowered.silica.content.blocks.AxisBlock;
import org.sandboxpowered.silica.content.blocks.SpreadingBlock;
import org.sandboxpowered.silica.content.blocks.StairsBlock;
import org.sandboxpowered.silica.content.fluid.EmptyFluid;

import java.util.Collections;
import java.util.Set;

import static org.sandboxpowered.api.block.Block.Settings.builder;

public class MinecraftAddon implements Addon {
    @Override
    public void init(SandboxAPI api) {
        api.getLog().info("Loading Minecraft Content - 1.16.4");
    }

    @Override
    public void register(SandboxAPI api, Registrar registrar) {
        registerBlocks(api, registrar);
        registerItems(api, registrar);
        registerFluids(api, registrar);
    }

    public void registerBlocks(SandboxAPI api, Registrar registrar) {
        Extra[] baseExtras = new Extra[]{Extra.SLAB, Extra.STAIRS};

        registrar.register("air", new AirBlock(builder(Material.AIR).build()));
        registerWithExtra(registrar, "stone", new BaseBlock(builder(Material.STONE).build()), baseExtras);
        registerWithExtra(registrar, "cobblestone", new BaseBlock(builder(Material.STONE).build()), baseExtras);
        registerWithExtra(registrar, "granite", new BaseBlock(builder(Material.STONE).build()), baseExtras);
        registerWithExtra(registrar, "diorite", new BaseBlock(builder(Material.STONE).build()), baseExtras);
        registerWithExtra(registrar, "andesite", new BaseBlock(builder(Material.STONE).build()), baseExtras);
        registrar.register("dirt", new BaseBlock(builder(Material.EARTH).build()));
        registrar.register("grass_block", new SpreadingBlock(builder(Material.EARTH).ticksRandomly().build()));
        registrar.register("bedrock", new BaseBlock(builder(Material.STONE).setStrength(-1).build()));
        registrar.register("sand", new BaseBlock(builder(Material.SAND).build()));
        registrar.register("red_sand", new BaseBlock(builder(Material.SAND).build()));


        for (Sandstone sandstone : Sandstone.values()) {
            registerWithExtra(registrar, sandstone.formatted("sandstone"), new BaseBlock(builder(Material.SAND).build()), sandstone != Sandstone.CHISELED ? baseExtras : null);
            registerWithExtra(registrar, sandstone.formatted("red_sandstone"), new BaseBlock(builder(Material.SAND).build()), sandstone != Sandstone.CHISELED ? baseExtras : null);
        }

        for (Wood wood : Wood.values()) {
            registrar.register(String.format("%s_log", wood.getPrefix()), new AxisBlock(builder(Material.WOOD).build()));
            registrar.register(String.format("stripped_%s_log", wood.getPrefix()), new AxisBlock(builder(Material.WOOD).build()));

            registrar.register(String.format("%s_wood", wood.getPrefix()), new AxisBlock(builder(Material.WOOD).build()));
            registrar.register(String.format("stripped_%s_wood", wood.getPrefix()), new AxisBlock(builder(Material.WOOD).build()));

            registerWithExtra(registrar, String.format("%s_planks", wood.getPrefix()), new BaseBlock(builder(Material.WOOD).build()), baseExtras);
        }
    }

    public void registerItems(SandboxAPI api, Registrar registrar) {

    }

    public void registerFluids(SandboxAPI api, Registrar registrar) {
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