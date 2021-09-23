package org.sandboxpowered.silica.vanilla.network.login.serverbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class LoginStart(private var username: String) : Packet {
    constructor(buf: PacketByteBuf) : this(buf.readString(16))

    override fun write(buf: PacketByteBuf) {
        buf.writeString(username)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        connection.handleLoginStart(username)
    }
}