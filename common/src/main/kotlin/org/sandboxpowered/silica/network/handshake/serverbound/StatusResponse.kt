package org.sandboxpowered.silica.network.handshake.serverbound

import org.sandboxpowered.silica.network.Connection
import org.sandboxpowered.silica.network.Packet
import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler

class StatusResponse(private var responseJson: String?) : Packet {

    constructor() : this(null)

    override fun read(buf: PacketByteBuf) {
        responseJson = buf.readString()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeString(responseJson!!)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}