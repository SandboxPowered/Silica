package org.sandboxpowered.silica

import com.artemis.BaseEntitySystem
import com.mojang.brigadier.CommandDispatcher
import org.apache.logging.log4j.LogManager
import org.sandboxpowered.silica.api.block.Block
import org.sandboxpowered.silica.api.command.CommandSource
import org.sandboxpowered.silica.api.entity.EntityDefinition
import org.sandboxpowered.silica.api.internal.InternalAPI
import org.sandboxpowered.silica.api.item.Item
import org.sandboxpowered.silica.api.network.NetworkAdapter
import org.sandboxpowered.silica.api.plugin.PluginManager
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.registry.RegistryEntry
import org.sandboxpowered.silica.api.server.Server
import org.sandboxpowered.silica.api.world.generation.WorldGenerator
import org.sandboxpowered.silica.registry.SilicaRegistries
import org.sandboxpowered.silica.world.SilicaWorld
import kotlin.reflect.KClass

class SilicaInternalAPI : InternalAPI {
    var networkAdapter: NetworkAdapter? = null
    private val log = LogManager.getLogger("SilicaAPI")
    override val commandDispatcher: CommandDispatcher<CommandSource> = CommandDispatcher()
    override val pluginManager: PluginManager
        get() = TODO("Not yet implemented")

    override fun registerNetworkAdapter(adapter: NetworkAdapter) {
        this.networkAdapter = adapter
        log.info("Registered network adapter ${adapter.id} for protocol ${adapter.protocol}")
        // TODO("Not yet implemented")
    }

    override fun registerWorldGenerator(generator: WorldGenerator) {
        log.info("Registered world generator ${generator.id}")
        SilicaWorld.worldGenerator = generator
        // TODO("Not yet implemented")
    }

    override fun <T : RegistryEntry<T>> getRegistry(kclass: KClass<T>): Registry<T> = when (kclass) {
        // TODO: change this to using a Map<Class, Registry>
        Block::class -> SilicaRegistries.BLOCK_REGISTRY.cast()
        Item::class -> SilicaRegistries.ITEM_REGISTRY.cast()
        EntityDefinition::class -> SilicaRegistries.ENTITY_DEFINITION_REGISTRY.cast()
        else -> error("Unknown registry type $kclass")
    }

    override fun registerSystem(system: BaseEntitySystem) {
        SilicaRegistries.SYSTEM_REGISTRY += system
    }

    override fun registerSystem(system: (Server) -> BaseEntitySystem) {
        SilicaRegistries.DYNAMIC_SYSTEM_REGISTRY += system
    }

    override fun registerCommands(builder: CommandDispatcher<CommandSource>.() -> Unit) {
        builder(commandDispatcher)
    }

    lateinit var registerListenerDelegate: (Any) -> Unit

    override fun registerListener(listener: Any) {
        this.registerListener(listener)
    }
}