package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class S2CUpdateEntityVelocity(
    var entityId: Int = -1,
    var vX: Short = 0,
    var vY: Short = 0,
    var vZ: Short = 0,
) : PacketPlay {

    constructor(buf: PacketBuffer) : this(
        buf.readVarInt(),
        buf.readShort(),
        buf.readShort(),
        buf.readShort()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(entityId)
        buf.writeShort(vX)
        buf.writeShort(vY)
        buf.writeShort(vZ)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}