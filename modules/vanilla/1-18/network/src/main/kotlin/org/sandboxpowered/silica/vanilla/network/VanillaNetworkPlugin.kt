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
import org.sandboxpowered.silica.vanilla.network.ecs.component.VanillaPlayerInputComponent
import org.sandboxpowered.silica.vanilla.network.ecs.system.VanillaInputSystem
import org.sandboxpowered.silica.vanilla.network.util.mapping.BlockStateProtocolMapping
import org.sandboxpowered.silica.vanilla.network.util.mapping.MappingErrorType
import org.sandboxpowered.silica.vanilla.network.util.mapping.VanillaProtocolMapping
import java.io.File
import java.nio.charset.StandardCharsets

@Plugin(
    id = "minecraft:network",
    version = "1.18",
    requirements = ["minecraft:content@1.18"],
    after = ["minecraft:content"],
    native = true
)
class VanillaNetworkPlugin : BasePlugin {
    private val logger = getLogger()

    override fun onEnable() {
        logger.info("Minecraft network adapter v1.18 enabled")

        val protocolErrors =
            VanillaProtocolMapping.INSTANCE.load() + mapOf("minecraft:blockstate" to BlockStateProtocolMapping.INSTANCE.load())
        if (protocolErrors.isNotEmpty()) {
            var totalMissing = 0
            var totalUnknown = 0
            protocolErrors.forEach { (registry, values) ->
                val fileSafeName = registry.replace('/', '_').replace(':', '-')
                val unknown = values[MappingErrorType.UNKNOWN]
                if (unknown != null && unknown.isNotEmpty()) {
                    totalUnknown += unknown.size
                    logger.trace("Found ${unknown.size} unknown elements in ${registry}. Exported to unknown/${fileSafeName}.txt")
                    val builder = StringBuilder()
                    unknown.sorted().forEach {
                        builder.append(it).append("\n")
                    }
                    FileUtils.writeStringToFile(
                        File("unknown/${fileSafeName}.txt"),
                        builder.toString(),
                        StandardCharsets.UTF_8
                    )
                }
                val missing = values[MappingErrorType.MISSING]
                if (missing != null && missing.isNotEmpty()) {
                    totalMissing += missing.size
                    logger.trace("Missing ${missing.size} vanilla elements in ${registry}. Exported to missing/${fileSafeName}.txt")
                    val builder = StringBuilder()
                    missing.sorted().forEach {
                        builder.append(it).append("\n")
                    }
                    FileUtils.writeStringToFile(
                        File("missing/${fileSafeName}.txt"),
                        builder.toString(),
                        StandardCharsets.UTF_8
                    )
                }
            }
            when {
                totalMissing > 0 && totalUnknown > 0 -> logger.error("$totalMissing Missing and $totalUnknown Unknown errors occurred, in the future Vanilla Network will be disabled in this situation.")
                totalMissing > 0 -> logger.error("$totalMissing Missing errors occurred, in the future Vanilla Network will be disabled in this situation.")
                totalUnknown > 0 -> logger.error("$totalUnknown Unknown errors occurred, in the future Vanilla Network will be disabled in this situation.")
            }
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
        logger.info("Minecraft network adapter v1.18 disabled!")
    }
}