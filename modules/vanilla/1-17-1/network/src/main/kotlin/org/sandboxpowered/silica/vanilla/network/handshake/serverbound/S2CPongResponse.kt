package org.sandboxpowered.silica.vanilla.network.handshake.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.HandledPacket
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class S2CPongResponse(private val time: Long) : HandledPacket {
    constructor(buf: PacketBuffer) : this(buf.readLong())

    override fun write(buf: PacketBuffer) {
        buf.writeLong(time)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}