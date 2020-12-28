package org.sandboxpowered.silica.content;

import org.sandboxpowered.api.SandboxAPI;
import org.sandboxpowered.api.addon.Addon;
import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Material;
import org.sandboxpowered.api.block.SlabBlock;
import org.sandboxpowered.api.registry.Registrar;
import org.sandboxpowered.silica.content.blocks.AirBlock;
import org.sandboxpowered.silica.content.blocks.AxisBlock;
import org.sandboxpowered.silica.content.blocks.SpreadingBlock;

import static org.sandboxpowered.api.block.Block.Settings.builder;

public class MinecraftAddon implements Addon {
    @Override
    public void init(SandboxAPI api) {
        api.getLog().info("Loading Minecraft Content - 1.16.4");
    }

    @Override
    public void register(SandboxAPI api, Registrar registrar) {
        registrar.register("air", new AirBlock(builder(Material.AIR).build()));
        registerWithSlab(registrar, "stone", new BaseBlock(builder(Material.STONE).build()));
        registerWithSlab(registrar, "cobblestone", new BaseBlock(builder(Material.STONE).build()));
        registerWithSlab(registrar, "granite", new BaseBlock(builder(Material.STONE).build()));
        registerWithSlab(registrar, "diorite", new BaseBlock(builder(Material.STONE).build()));
        registerWithSlab(registrar, "andesite", new BaseBlock(builder(Material.STONE).build()));
        registrar.register("dirt", new BaseBlock(builder(Material.EARTH).build()));
        registrar.register("grass_block", new SpreadingBlock(builder(Material.EARTH).ticksRandomly().build()));
        registrar.register("bedrock", new BaseBlock(builder(Material.STONE).setStrength(-1).build()));
        registrar.register("sand", new BaseBlock(builder(Material.SAND).build()));
        registrar.register("red_sand", new BaseBlock(builder(Material.SAND).build()));

        for (Sandstone sandstone : Sandstone.values()) {
            if (sandstone != Sandstone.CHISELED) {
                registerWithSlab(registrar, sandstone.formatted("sandstone"), new BaseBlock(builder(Material.SAND).build()));
                registerWithSlab(registrar, sandstone.formatted("red_sandstone"), new BaseBlock(builder(Material.SAND).build()));
            } else {
                registrar.register(sandstone.formatted("sandstone"), new BaseBlock(builder(Material.SAND).build()));
                registrar.register(sandstone.formatted("red_sandstone"), new BaseBlock(builder(Material.SAND).build()));
            }
        }

        for (Wood wood : Wood.values()) {
            registrar.register(String.format("%s_log", wood.getPrefix()), new AxisBlock(builder(Material.WOOD).build()));
            registrar.register(String.format("stripped_%s_log", wood.getPrefix()), new AxisBlock(builder(Material.WOOD).build()));

            registrar.register(String.format("%s_wood", wood.getPrefix()), new AxisBlock(builder(Material.WOOD).build()));
            registrar.register(String.format("stripped_%s_wood", wood.getPrefix()), new AxisBlock(builder(Material.WOOD).build()));

            registerWithSlab(registrar, String.format("%s_planks", wood.getPrefix()), new BaseBlock(builder(Material.WOOD).build()));
        }
    }

    public void registerWithSlab(Registrar registrar, String name, BaseBlock block) {
        registrar.register(name, block);
        registrar.register(String.format("%s_slab", name), new SlabBlock(builder(block).build()));
    }
}