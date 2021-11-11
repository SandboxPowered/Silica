package org.sandboxpowered.silica.registry

import org.reflections.Reflections
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.block.BlockEntityProvider
import org.sandboxpowered.silica.api.fluid.Fluid
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.registry.RegistryDelegate
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.util.extensions.getAnnotation
import org.sandboxpowered.silica.util.extensions.getTypesAnnotatedWith

object SilicaRegistries {
    val BLOCK_REGISTRY = SilicaRegistry(Identifier("minecraft", "block"), Block::class.java).apply {
        addListener {
            if (it is BlockEntityProvider)
                BLOCKS_WITH_ENTITY.add(it)
        }
    }

    val BLOCKS_WITH_ENTITY = ArrayList<BlockEntityProvider>()

    val ITEM_REGISTRY = SilicaRegistry(Identifier("minecraft", "item"), Item::class.java)

    val FLUID_REGISTRY = SilicaRegistry(Identifier("minecraft", "fluid"), Fluid::class.java)

    init {
        //TODO: make this use an plugin classloader rather than package search
        val classes = Reflections("org.sandboxpowered").getTypesAnnotatedWith<Plugin>()
        val log = getLogger()
        log.info("Loading ${classes.size} plugins")
        classes.forEach {
            val plugin = it.getAnnotation<Plugin>()
            if (BasePlugin::class.java.isAssignableFrom(it)) {
                val instance = it.getConstructor().newInstance() as BasePlugin
                log.debug("Loading ${plugin.id}@${plugin.version}")
                instance.onEnable()
            }
        }
    }

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