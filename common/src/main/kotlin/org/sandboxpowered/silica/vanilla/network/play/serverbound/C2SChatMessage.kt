package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.ecs.component.PositionComponent
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.registry.Registries
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.api.util.extensions.create
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.world.SilicaWorld.Command.DelayedCommand.Companion.spawnEntity

data class C2SChatMessage(private val message: String) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readString(MAX_SIZE))

    override fun write(buf: PacketBuffer) {
        buf.writeString(message.take(MAX_SIZE))
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        if (message.startsWith("/spawn")) {
            val parts = message.split(' ')
            if (parts.size >= 5) {
                val entity = Registries.ENTITY_DEFINITIONS[Identifier(parts[1])].orNull()
                if (entity != null) context.world.tell(spawnEntity(entity) {
                    val pos = it.create<PositionComponent>().pos
                    pos.set(parts[2].toDouble() + .5, parts[3].toDouble(), parts[4].toDouble() + .5)
                })
            }

        } else logger.info(this)
        // TODO: handle chat message
    }

    private companion object {
        private const val MAX_SIZE = 256
    }
}