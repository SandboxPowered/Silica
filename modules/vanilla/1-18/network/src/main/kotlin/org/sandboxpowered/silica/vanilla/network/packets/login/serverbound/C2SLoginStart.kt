package org.sandboxpowered.silica.vanilla.network.packets.login.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.HandledPacket

class C2SLoginStart(private val username: String) : HandledPacket {
    constructor(buf: PacketBuffer) : this(buf.readString(16))

    override fun write(buf: PacketBuffer) {
        buf.writeString(username)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        connection.handleLoginStart(username)
    }
}