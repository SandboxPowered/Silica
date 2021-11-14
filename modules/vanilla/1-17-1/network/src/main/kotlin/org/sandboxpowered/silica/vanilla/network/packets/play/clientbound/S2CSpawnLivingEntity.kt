package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.util.extensions.readAngle
import org.sandboxpowered.silica.vanilla.network.util.extensions.writeAngle
import java.util.*

class S2CSpawnLivingEntity(
    private val entityId: Int,
    private val uuid: UUID,
    private val type: Int,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val yaw: Float,
    private val pitch: Float,
    private val headPitch: Byte,
    private val velocityX: Short,
    private val velocityY: Short,
    private val velocityZ: Short
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(
        buf.readVarInt(),
        buf.readUUID(),
        buf.readVarInt(),
        buf.readDouble(),
        buf.readDouble(),
        buf.readDouble(),
        buf.readAngle(),
        buf.readAngle(),
        buf.readByte(),
        buf.readShort(),
        buf.readShort(),
        buf.readShort()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(entityId)
        buf.writeUUID(uuid)
        buf.writeVarInt(type)
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeAngle(yaw)
        buf.writeAngle(pitch)
        buf.writeByte(headPitch)
        buf.writeShort(velocityX)
        buf.writeShort(velocityY)
        buf.writeShort(velocityZ)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}