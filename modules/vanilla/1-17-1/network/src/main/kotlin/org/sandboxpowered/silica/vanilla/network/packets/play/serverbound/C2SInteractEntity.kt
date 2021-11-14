package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.joml.Vector3f
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

/**
 * @param type 0: interact, 1: attack, 2: interact at
 */
data class C2SInteractEntity(
    private val entityId: Int,
    private val type: Int,
    private val target: Vector3f?,
    private val hand: Int?,
    private val sneaking: Boolean
) : PacketPlay {
    init {
        require((type == 2) == (target !== null))
        require((type == 1) == (hand === null))
    }

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(entityId)
        buf.writeVarInt(type)
        target?.let(buf::writeVector3f)
        hand?.let(buf::writeVarInt)
        buf.writeBoolean(sneaking)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle interact with entity
    }

    companion object {
        fun readFrom(buf: PacketBuffer): C2SInteractEntity {
            val entityId = buf.readVarInt()
            val type = buf.readVarInt()
            val target = if (type == 2) buf.readVector3f() else null
            val hand = if (type != 1) buf.readVarInt() else null
            val sneaking = buf.readBoolean()
            return C2SInteractEntity(entityId, type, target, hand, sneaking)
        }
    }
}