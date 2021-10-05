package org.sandboxpowered.silica.vanilla.network.login.clientbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class S2CDisconnect(private val reason: String) : Packet {
    constructor(buf: PacketByteBuf) : this(buf.readString())

    override fun write(buf: PacketByteBuf) {
        buf.writeString(reason)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}