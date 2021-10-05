package org.sandboxpowered.silica.vanilla.network.handshake.clientbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.handshake.serverbound.S2CPongResponse

class C2SPingRequest(val time: Long) : Packet {
    constructor(buf: PacketByteBuf) : this(buf.readLong())

    override fun write(buf: PacketByteBuf) {
        buf.writeLong(time)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        packetHandler.sendPacket(S2CPongResponse(time))
    }
}