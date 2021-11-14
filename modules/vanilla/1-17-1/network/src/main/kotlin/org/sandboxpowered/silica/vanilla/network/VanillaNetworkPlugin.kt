package org.sandboxpowered.silica.vanilla.network

import org.apache.commons.io.FileUtils
import org.sandboxpowered.silica.api.SilicaAPI
import org.sandboxpowered.silica.api.ecs.component.PlayerComponent
import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.entity.EntityEvents
import org.sandboxpowered.silica.api.plugin.BasePlugin
import org.sandboxpowered.silica.api.plugin.Plugin
import org.sandboxpowered.silica.api.util.extensions.add
import org.sandboxpowered.silica.api.util.extensions.getComponent
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.util.mapping.BlockStateProtocolMapping.ErrorType
import org.sandboxpowered.silica.vanilla.network.ecs.system.VanillaInputSystem
import org.sandboxpowered.silica.vanilla.network.ecs.component.VanillaPlayerInputComponent
import org.sandboxpowered.silica.vanilla.network.util.mapping.BlockStateProtocolMapping
import org.sandboxpowered.silica.vanilla.network.util.mapping.VanillaProtocolMapping
import java.io.File
import java.nio.charset.StandardCharsets

@Plugin(
    id = "minecraft:network",
    version = "1.17.1",
    requirements = ["minecraft:content@1.17.1"],
    after = ["minecraft:content"],
    native = true
)
class VanillaNetworkPlugin : BasePlugin {
    private val logger = getLogger()
    override fun onEnable() {
        logger.info("Minecraft network adapter v1.17.1 enabled!")

        val stateMappingErrors = BlockStateProtocolMapping.INSTANCE.load()
        VanillaProtocolMapping.INSTANCE.load()
        if (stateMappingErrors.isNotEmpty()) {
            val unknown = stateMappingErrors[ErrorType.UNKNOWN]
            if (unknown != null && unknown.isNotEmpty()) {
                logger.error("Found ${unknown.size} unknown BlockStates. Exported to unknown.txt")
                val builder = StringBuilder()
                unknown.sorted().forEach {
                    builder.append(it).append("\n")
                }
                FileUtils.writeStringToFile(File("unknown.txt"), builder.toString(), StandardCharsets.UTF_8)
            }
            val missing = stateMappingErrors[ErrorType.MISSING]
            if (missing != null && missing.isNotEmpty()) {
                logger.error("Missing ${missing.size} vanilla BlockStates. Exported to missing.txt")
                val builder = StringBuilder()
                missing.sorted().forEach {
                    builder.append(it).append("\n")
                }
                FileUtils.writeStringToFile(File("missing.txt"), builder.toString(), StandardCharsets.UTF_8)
            }
            logger.error("State errors occurred, in the future Vanilla Network will be disabled in this situation.")
            //TODO: Disable Vanilla Network
        } else {
            logger.info("Accepting vanilla connections")
        }
        EntityEvents.INITIALIZE_ARCHETYPE_EVENT.subscribe { ent, edit ->
            if (ent.identifier.path == "player") {
                edit.add<VanillaPlayerInputComponent>()
            }
        }
        EntityEvents.SPAWN_ENTITY_EVENT.subscribe {
            val component = it.getComponent<VanillaPlayerInputComponent>()
            if (component != null) {
                val player = it.getComponent<PlayerComponent>()!!
                val position = it.getComponent<PositionComponent>()!!
                component.initialize(it.id, player.profile!!)
                component.wantedPosition.set(position.pos)
            }
        }
        SilicaAPI.registerSystem(::VanillaInputSystem)
        SilicaAPI.registerNetworkAdapter(VanillaNetworkAdapter)
    }

    override fun onDisable() {
        logger.info("Minecraft network adapter v1.17.1 disabled!")
    }
}