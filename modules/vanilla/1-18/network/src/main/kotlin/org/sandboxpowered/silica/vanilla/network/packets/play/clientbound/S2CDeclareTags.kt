package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.network.writeCollection
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.registry.Registry
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.util.mapping.VanillaProtocolMapping
import kotlin.system.measureTimeMillis

class S2CDeclareTags() : PacketPlay {
    constructor(buf: PacketBuffer) : this()

    private val logger = getLogger()

    override fun write(buf: PacketBuffer) {
        val time = measureTimeMillis {
            buf.writeVarInt(5)
            write(buf, "block", Registries.BLOCKS)
            write(buf, "item", Registries.ITEMS)
            write(buf, "fluid", Registries.FLUIDS)
            write(buf, "entity_type", Registries.ENTITY_DEFINITIONS)
            write(buf, "game_event", Registries.GAME_EVENTS)
        }

        logger.info("Serialized tags in $time millis")
    }

    private fun write(buf: PacketBuffer, type: String, registry: Registry<*>) {
        val mapper = VanillaProtocolMapping.INSTANCE["minecraft:$type"]
        buf.writeIdentifier(Identifier(type))
        buf.writeCollection(registry.tags) { identity ->
            buf.writeIdentifier(identity)
            buf.writeCollection(registry.getByTag(identity).orEmpty()) {
                writeVarInt(mapper[it.id])
            }
        }
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}