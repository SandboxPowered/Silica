package org.sandboxpowered.silica.network.play.clientbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class SetPlayerPositionAndLook(
    private var x: Double = 0.0, private var y: Double = 0.0, private var z: Double = 0.0,
    private var yaw: Float = 0f, private var pitch: Float = 0f, private var flags: Byte = 0,
    private var id: Int = 0,
) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        x = buf.readDouble()
        y = buf.readDouble()
        z = buf.readDouble()
        yaw = buf.readFloat()
        pitch = buf.readFloat()
        flags = buf.readByte()
        id = buf.readVarInt()
    }

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