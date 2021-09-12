package org.sandboxpowered.silica.network.handshake.clientbound

import org.sandboxpowered.silica.network.Connection
import org.sandboxpowered.silica.network.Packet
import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.handshake.serverbound.PongResponse

class PingRequest(private var time: Long) : Packet {

    constructor() : this(-1)

    override fun read(buf: PacketByteBuf) {
        time = buf.readLong()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeLong(time)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        packetHandler.sendPacket(PongResponse(time))
    }
}