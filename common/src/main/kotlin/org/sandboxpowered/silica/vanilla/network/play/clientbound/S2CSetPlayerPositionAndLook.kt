package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CSetPlayerPositionAndLook(
    private val x: Double, private val y: Double, private val z: Double,
    private val yaw: Float, private val pitch: Float, private val flags: Byte,
    private val id: Int,
) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(
        buf.readDouble(),
        buf.readDouble(),
        buf.readDouble(),
        buf.readFloat(),
        buf.readFloat(),
        buf.readByte(),
        buf.readVarInt()
    )

    override fun write(buf: PacketByteBuf) {
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeFloat(yaw)
        buf.writeFloat(pitch)
        buf.writeByte(flags.toInt())
        buf.writeVarInt(id)
        buf.writeBoolean(false) // dismount vehicle
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}