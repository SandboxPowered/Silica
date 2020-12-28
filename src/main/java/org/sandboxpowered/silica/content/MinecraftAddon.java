package org.sandboxpowered.silica.content;

import org.sandboxpowered.api.SandboxAPI;
import org.sandboxpowered.api.addon.Addon;
import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.block.Material;
import org.sandboxpowered.api.registry.Registrar;
import org.sandboxpowered.silica.content.blocks.AxisBlock;
import org.sandboxpowered.silica.content.blocks.SpreadingBlock;

public class MinecraftAddon implements Addon {
    @Override
    public void init(SandboxAPI api) {
        api.getLog().info("Loading Minecraft Content - 1.16.4");
    }

    @Override
    public void register(SandboxAPI api, Registrar registrar) {
        registrar.register("stone", new BaseBlock(Block.Settings.builder(Material.STONE).build()));
        registrar.register("dirt", new BaseBlock(Block.Settings.builder(Material.EARTH).build()));
        registrar.register("grass_block", new SpreadingBlock(Block.Settings.builder(Material.EARTH).ticksRandomly().build()));
        registrar.register("bedrock", new BaseBlock(Block.Settings.builder(Material.STONE).setStrength(-1).build()));
        registrar.register("sand", new BaseBlock(Block.Settings.builder(Material.SAND).build()));
        registrar.register("red_sand", new BaseBlock(Block.Settings.builder(Material.SAND).build()));

        for (Sandstone sandstone : Sandstone.values()) {
            registrar.register(sandstone.getPrefix() == null ? "sandstone" : String.format("%s_sandstone", sandstone.getPrefix()), new BaseBlock(Block.Settings.builder(Material.SAND).build()));
            registrar.register(sandstone.getPrefix() == null ? "red_sandstone" : String.format("%s_red_sandstone", sandstone.getPrefix()), new BaseBlock(Block.Settings.builder(Material.SAND).build()));
        }

        for (Wood wood : Wood.values()) {
            registrar.register(String.format("%s_log", wood.getPrefix()), new AxisBlock(Block.Settings.builder(Material.WOOD).build()));
            registrar.register(String.format("stripped_%s_log", wood.getPrefix()), new AxisBlock(Block.Settings.builder(Material.WOOD).build()));

            registrar.register(String.format("%s_wood", wood.getPrefix()), new AxisBlock(Block.Settings.builder(Material.WOOD).build()));
            registrar.register(String.format("stripped_%s_wood", wood.getPrefix()), new AxisBlock(Block.Settings.builder(Material.WOOD).build()));

            registrar.register(String.format("%s_planks", wood.getPrefix()), new BaseBlock(Block.Settings.builder(Material.WOOD).build()));
        }
    }
}