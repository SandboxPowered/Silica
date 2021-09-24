package org.sandboxpowered.silica.vanilla.network.login.clientbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import java.util.*

class LoginSuccess(private var uuid: UUID, private var username: String) : Packet {
    constructor(buf: PacketByteBuf) : this(buf.readUUID(), buf.readString(16))

    override fun write(buf: PacketByteBuf) {
        buf.writeUUID(uuid)
        buf.writeString(username)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}