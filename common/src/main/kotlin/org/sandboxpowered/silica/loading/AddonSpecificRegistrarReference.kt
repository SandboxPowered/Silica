package org.sandboxpowered.silica.loading

import org.sandboxpowered.api.addon.AddonInfo
import org.sandboxpowered.api.block.BaseBlock
import org.sandboxpowered.api.block.Block
import org.sandboxpowered.api.content.Content
import org.sandboxpowered.api.fluid.BaseFluid
import org.sandboxpowered.api.fluid.Fluid
import org.sandboxpowered.api.item.BaseBlockItem
import org.sandboxpowered.api.registry.Registrar
import org.sandboxpowered.api.registry.Registry
import org.sandboxpowered.api.state.BlockState
import org.sandboxpowered.api.state.FluidState
import org.sandboxpowered.api.util.Identity
import org.sandboxpowered.internal.AddonSpec
import org.sandboxpowered.silica.state.SilicaStateBuilder
import org.sandboxpowered.silica.state.SilicaStateFactory
import org.sandboxpowered.silica.state.block.SilicaBlockState
import org.sandboxpowered.silica.state.fluid.SilicaFluidState
import java.util.*

class AddonSpecificRegistrarReference(private val spec: AddonSpec, private val loader: SandboxLoader) : Registrar {
    override fun getSourceAddon(): AddonInfo = spec

    override fun <T : Content<T>> getEntry(identity: Identity, tClass: Class<T>): Registry.Entry<T> {
        return Registry.getRegistryFromType(tClass)[identity]
    }

    override fun <T : Content<T>> getEntry(identity: Identity, registry: Registry<T>): Registry.Entry<T> {
        return registry[identity]
    }

    override fun <T : Content<T>> register(content: T): Registry.Entry<T> {
        val entry = Registry.getRegistryFromType(content.contentType).register(content)
        if (content is BaseBlock) {
            val builder = SilicaStateBuilder<Block, BlockState>(content as Block)
            (content as BaseBlock).appendProperties(builder)
            (content as BaseBlock).stateFactory = SilicaStateFactory(
                content as Block,
                builder.getProperties(),
                SilicaStateFactory.Factory.of { base, properties -> SilicaBlockState(base, properties) })
            val item = (content as BaseBlock).createBlockItem()
            if (item is BaseBlockItem) {
                register(item.setIdentity(content.getIdentity()))
            }
        }
        if (content is BaseFluid) {
            val builder = SilicaStateBuilder<Fluid, FluidState>(content as Fluid)
            (content as BaseFluid).appendProperties(builder)
            (content as BaseFluid).stateFactory = SilicaStateFactory(
                content as Fluid,
                builder.getProperties(),
                SilicaStateFactory.Factory.of { base, properties -> SilicaFluidState(base, properties) })
        }
        return entry
    }

    override fun <T : Registrar.Service?> getRegistrarService(tClass: Class<T>): Optional<T> {
        return Optional.empty()
    }
}