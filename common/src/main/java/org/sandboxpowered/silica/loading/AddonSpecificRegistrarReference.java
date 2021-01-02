package org.sandboxpowered.silica.loading;

import org.sandboxpowered.api.addon.AddonInfo;
import org.sandboxpowered.api.block.BaseBlock;
import org.sandboxpowered.api.block.Block;
import org.sandboxpowered.api.content.Content;
import org.sandboxpowered.api.fluid.BaseFluid;
import org.sandboxpowered.api.fluid.Fluid;
import org.sandboxpowered.api.item.BaseBlockItem;
import org.sandboxpowered.api.item.BlockItem;
import org.sandboxpowered.api.registry.Registrar;
import org.sandboxpowered.api.registry.Registry;
import org.sandboxpowered.api.state.BlockState;
import org.sandboxpowered.api.state.FluidState;
import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.internal.AddonSpec;
import org.sandboxpowered.silica.block.SilicaBlockState;
import org.sandboxpowered.silica.fluid.SilicaFluidState;
import org.sandboxpowered.silica.state.SilicaStateBuilder;
import org.sandboxpowered.silica.state.SilicaStateFactory;

import java.util.Optional;

public class AddonSpecificRegistrarReference implements Registrar {
    private final AddonSpec spec;
    private final SandboxLoader loader;

    public AddonSpecificRegistrarReference(AddonSpec spec, SandboxLoader loader) {
        this.spec = spec;
        this.loader = loader;
    }

    @Override
    public AddonInfo getSourceAddon() {
        return spec;
    }

    @Override
    public <T extends Content<T>> Registry.Entry<T> getEntry(Identity identity, Class<T> tClass) {
        return Registry.getRegistryFromType(tClass).get(identity);
    }

    @Override
    public <T extends Content<T>> Registry.Entry<T> getEntry(Identity identity, Registry<T> registry) {
        return registry.get(identity);
    }

    @Override
    public <T extends Content<T>> Registry.Entry<T> register(T content) {
        Registry.Entry<T> entry = Registry.getRegistryFromType(content.getContentType()).register(content);
        if (content instanceof BaseBlock) {
            SilicaStateBuilder<Block, BlockState> builder = new SilicaStateBuilder<>((Block) content);
            ((BaseBlock) content).appendProperties(builder);
            ((BaseBlock) content).setStateFactory(new SilicaStateFactory<>((Block) content, builder.getProperties(), SilicaBlockState::new));
            BlockItem item = ((BaseBlock) content).createBlockItem();
            if (item instanceof BaseBlockItem) {
                register(item.setIdentity(content.getIdentity()));
            }
        }
        if (content instanceof BaseFluid) {
            SilicaStateBuilder<Fluid, FluidState> builder = new SilicaStateBuilder<>((Fluid) content);
            ((BaseFluid) content).appendProperties(builder);
            ((BaseFluid) content).setStateFactory(new SilicaStateFactory<>((Fluid) content, builder.getProperties(), SilicaFluidState::new));
        }
        return entry;
    }

    @Override
    public <T extends Service> Optional<T> getRegistrarService(Class<T> tClass) {
        return Optional.empty();
    }
}