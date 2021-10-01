package org.sandboxpowered.silica.vanilla.network.login.clientbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class EncryptionRequest(
    private val serverId: String,
    private val publicKey: ByteArray,
    private val verifyArray: ByteArray
) : Packet {
    constructor(buf: PacketByteBuf) : this(buf.readString(20), buf.readByteArray(), buf.readByteArray())

    override fun write(buf: PacketByteBuf) {
        buf.writeString(serverId)
        buf.writeByteArray(publicKey)
        buf.writeByteArray(verifyArray)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}