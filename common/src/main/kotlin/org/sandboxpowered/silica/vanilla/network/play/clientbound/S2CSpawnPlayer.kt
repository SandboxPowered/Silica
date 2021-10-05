package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import java.util.*

class S2CSpawnPlayer(
    var entityId: Int = -1,
    var uuid: UUID? = null,
    var x: Double = -1.0,
    var y: Double = -1.0,
    var z: Double = -1.0,
    var yaw: Byte = 0,
    var pitch: Byte = 0
) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        TODO("Not yet implemented")
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(entityId)
        buf.writeUUID(uuid!!)
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeByte(yaw)
        buf.writeByte(pitch)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}