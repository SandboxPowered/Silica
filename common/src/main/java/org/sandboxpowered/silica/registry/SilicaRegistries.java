package org.sandboxpowered.silica.registry;

import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.fluid.Fluid;
import org.sandboxpowered.api.item.Item;
import org.sandboxpowered.api.util.Identity;

public class SilicaRegistries {
    public static final SilicaRegistry<Block> BLOCK_REGISTRY = new SilicaRegistry<>(Identity.of("minecraft", "block"), Block.class);
    public static final SilicaRegistry<Item> ITEM_REGISTRY = new SilicaRegistry<>(Identity.of("minecraft", "item"), Item.class);
    public static final SilicaRegistry<Fluid> FLUID_REGISTRY = new SilicaRegistry<>(Identity.of("minecraft", "fluid"), Fluid.class);
}
