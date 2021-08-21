package org.sandboxpowered.silica.network.login.clientbound

import org.sandboxpowered.silica.network.Connection
import org.sandboxpowered.silica.network.Packet
import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import java.util.*

class LoginSuccess(private var uuid: UUID?, private var username: String?) : Packet {

    constructor() : this(null, null)

    override fun read(buf: PacketByteBuf) {
        uuid = buf.readUUID()
        username = buf.readString(16)
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeUUID(uuid!!)
        buf.writeString(username!!)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}