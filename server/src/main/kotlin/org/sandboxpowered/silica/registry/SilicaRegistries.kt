package org.sandboxpowered.silica.registry

import com.artemis.BaseEntitySystem
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.block.BlockEntityProvider
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.fluid.Fluid
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.registry.RegistryDelegate
import org.sandboxpowered.silica.api.server.Server
import org.sandboxpowered.silica.api.Identifier

object SilicaRegistries {
    val BLOCK_REGISTRY = SilicaRegistry(Identifier("minecraft", "block"), Block::class.java).apply {
        addListener {
            if (it is BlockEntityProvider)
                BLOCKS_WITH_ENTITY.add(it)
        }
    }

    val BLOCKS_WITH_ENTITY = ArrayList<BlockEntityProvider>()

    val ITEM_REGISTRY = SilicaRegistry(Identifier("minecraft", "item"), Item::class.java)

    val ENTITY_DEFINITION_REGISTRY =
        SilicaRegistry(Identifier("minecraft", "entity_type"), EntityDefinition::class.java)

    val FLUID_REGISTRY = SilicaRegistry(Identifier("minecraft", "fluid"), Fluid::class.java)

    val SYSTEM_REGISTRY = mutableSetOf<BaseEntitySystem>() // TODO : make an actual registry for this
    val DYNAMIC_SYSTEM_REGISTRY =
        mutableSetOf<(Server) -> BaseEntitySystem>() // TODO : make an actual registry for this

    private val blockDelegates = HashMap<String, RegistryDelegate<Block>>()
    private val itemDelegates = HashMap<String, RegistryDelegate<Item>>()
    private val fluidDelegates = HashMap<String, RegistryDelegate<Fluid>>()

    fun blocks(domain: String = "minecraft"): RegistryDelegate<Block> =
        blockDelegates.computeIfAbsent(domain) { RegistryDelegate(BLOCK_REGISTRY, it) }

    fun items(domain: String = "minecraft"): RegistryDelegate<Item> =
        itemDelegates.computeIfAbsent(domain) { RegistryDelegate(ITEM_REGISTRY, it) }

    fun fluids(domain: String = "minecraft"): RegistryDelegate<Fluid> =
        fluidDelegates.computeIfAbsent(domain) { RegistryDelegate(FLUID_REGISTRY, it) }
}