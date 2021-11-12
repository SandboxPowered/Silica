package org.sandboxpowered.silica.vanilla.network.login.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import java.util.*

class S2CLoginSuccess(private val uuid: UUID, private val username: String) : Packet {
    constructor(buf: PacketBuffer) : this(buf.readUUID(), buf.readString(16))

    override fun write(buf: PacketBuffer) {
        buf.writeUUID(uuid)
        buf.writeString(username)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}