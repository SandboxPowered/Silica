package org.sandboxpowered.silica.network.login.serverbound

import org.sandboxpowered.silica.network.Connection
import org.sandboxpowered.silica.network.Packet
import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler

class LoginStart(private var username: String?) : Packet {

    constructor() : this(null)

    override fun read(buf: PacketByteBuf) {
        username = buf.readString(16)
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeString(username!!)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        connection.handleLoginStart(username!!)
    }
}