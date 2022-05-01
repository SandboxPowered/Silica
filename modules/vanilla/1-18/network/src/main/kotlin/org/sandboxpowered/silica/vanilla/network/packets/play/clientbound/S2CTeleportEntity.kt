package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.util.extensions.readAngle
import org.sandboxpowered.silica.vanilla.network.util.extensions.writeAngle

class S2CTeleportEntity(
    val entityId: Int,
    val posX: Double,
    val posY: Double,
    val posZ: Double,
    val yaw: Float,
    val pitch: Float,
    val onGround: Boolean
) : PacketPlay {

    constructor(buf: PacketBuffer) : this(
        buf.readVarInt(),
        buf.readDouble(),
        buf.readDouble(),
        buf.readDouble(),
        buf.readAngle(),
        buf.readAngle(),
        buf.readBoolean(),
    )

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(entityId)
        buf.writeDouble(posX)
        buf.writeDouble(posY)
        buf.writeDouble(posZ)
        buf.writeAngle(yaw)
        buf.writeAngle(pitch)
        buf.writeBoolean(onGround)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}