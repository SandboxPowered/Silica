package org.sandboxpowered.silica.network.login.serverbound

import org.sandboxpowered.silica.network.Connection
import org.sandboxpowered.silica.network.Packet
import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.Protocol.Companion.getProtocolFromId

class HandshakeRequest(
    private var protocolVersion: Int,
    private var hostName: String?,
    private var port: Int,
    private var intention: Int
) : Packet {

    constructor() : this(0, null, 0, 0)

    override fun read(buf: PacketByteBuf) {
        protocolVersion = buf.readVarInt()
        hostName = buf.readString(255)
        port = buf.readUnsignedShort()
        intention = buf.readVarInt()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(protocolVersion)
        buf.writeString(hostName!!)
        buf.writeShort(port)
        buf.writeVarInt(intention)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        val protocol = getProtocolFromId(intention)
        packetHandler.setProtocol(protocol)
    }
}