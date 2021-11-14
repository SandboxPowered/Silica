package org.sandboxpowered.silica.vanilla.network.packets.login.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.packets.HandledPacket
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class S2CEncryptionRequest(
    private val serverId: String,
    private val publicKey: ByteArray,
    private val verifyArray: ByteArray
) : HandledPacket {
    constructor(buf: PacketBuffer) : this(buf.readString(20), buf.readByteArray(), buf.readByteArray())

    override fun write(buf: PacketBuffer) {
        buf.writeString(serverId)
        buf.writeByteArray(publicKey)
        buf.writeByteArray(verifyArray)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}