package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.util.extensions.readAngle
import org.sandboxpowered.silica.vanilla.network.util.extensions.writeAngle

class S2CUpdateEntityRotation(
    private val entityId: Int,
    private val yaw: Float,
    private val pitch: Float,
    private val onGround: Boolean
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(
        buf.readVarInt(),
        buf.readAngle(),
        buf.readAngle(),
        buf.readBoolean()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(entityId)
        buf.writeAngle(yaw)
        buf.writeAngle(pitch)
        buf.writeBoolean(onGround)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}