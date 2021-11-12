package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CEntityStatus(private val entityId: Int, private val status: Byte) : PacketPlay {

    constructor(buf: PacketByteBuf) : this(buf.readInt(), buf.readByte())

    override fun write(buf: PacketByteBuf) {
        buf.writeInt(entityId)
        buf.writeByte(status)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}