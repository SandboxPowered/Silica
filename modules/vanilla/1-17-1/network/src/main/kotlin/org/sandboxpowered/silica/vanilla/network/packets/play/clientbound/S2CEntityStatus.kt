package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CEntityStatus(private val entityId: Int, private val status: Byte) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readInt(), buf.readByte())

    override fun write(buf: PacketBuffer) {
        buf.writeInt(entityId)
        buf.writeByte(status)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}