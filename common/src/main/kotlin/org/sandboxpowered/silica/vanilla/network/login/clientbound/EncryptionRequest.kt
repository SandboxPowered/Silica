package org.sandboxpowered.silica.vanilla.network.login.clientbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class EncryptionRequest(
    private var serverId: String?,
    private var publicKey: ByteArray?,
    private var verifyArray: ByteArray?
) : Packet {

    constructor() : this(null, null, null)

    override fun read(buf: PacketByteBuf) {
        serverId = buf.readString(20)
        publicKey = buf.readByteArray()
        verifyArray = buf.readByteArray()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeString(serverId!!)
        buf.writeByteArray(publicKey!!)
        buf.writeByteArray(verifyArray!!)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}