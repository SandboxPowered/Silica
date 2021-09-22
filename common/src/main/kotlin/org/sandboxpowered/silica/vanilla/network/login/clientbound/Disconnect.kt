package org.sandboxpowered.silica.vanilla.network.login.clientbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class Disconnect(private var reason: String?) : Packet {

    constructor() : this(null)

    override fun read(buf: PacketByteBuf) {
        reason = buf.readString()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeString(reason!!)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}