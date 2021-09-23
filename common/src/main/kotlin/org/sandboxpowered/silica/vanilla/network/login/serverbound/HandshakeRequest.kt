package org.sandboxpowered.silica.vanilla.network.login.serverbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.Protocol.Companion.getProtocolFromId

class HandshakeRequest(
    private var protocolVersion: Int,
    private var hostName: String,
    private var port: UShort,
    private var intention: Int,
) : Packet {

    constructor(buf: PacketByteBuf) : this(buf.readVarInt(), buf.readString(255), buf.readUShort(), buf.readVarInt())

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(protocolVersion)
        buf.writeString(hostName)
        buf.writeUShort(port)
        buf.writeVarInt(intention)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        val protocol = getProtocolFromId(intention)
        packetHandler.setProtocol(protocol)
    }
}