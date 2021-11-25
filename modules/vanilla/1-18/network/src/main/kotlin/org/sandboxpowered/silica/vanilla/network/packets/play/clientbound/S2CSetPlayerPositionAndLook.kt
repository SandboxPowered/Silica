package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class S2CSetPlayerPositionAndLook(
    private val x: Double, private val y: Double, private val z: Double,
    private val yaw: Float, private val pitch: Float, private val flags: Byte,
    private val id: Int,
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(
        buf.readDouble(),
        buf.readDouble(),
        buf.readDouble(),
        buf.readFloat(),
        buf.readFloat(),
        buf.readByte(),
        buf.readVarInt()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeFloat(yaw)
        buf.writeFloat(pitch)
        buf.writeByte(flags)
        buf.writeVarInt(id)
        buf.writeBoolean(false) // dismount vehicle
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}