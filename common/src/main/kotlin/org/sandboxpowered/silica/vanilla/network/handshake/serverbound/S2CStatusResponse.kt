package org.sandboxpowered.silica.vanilla.network.handshake.serverbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class S2CStatusResponse(private val responseJson: String) : Packet {
    constructor(buf: PacketByteBuf) : this(buf.readString())

    override fun write(buf: PacketByteBuf) {
        buf.writeString(responseJson)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}