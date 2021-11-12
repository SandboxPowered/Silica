package org.sandboxpowered.silica.vanilla.network.login.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.Protocol.Companion.getProtocolFromId

class C2SHandshakeRequest(
    private val protocolVersion: Int,
    private val hostName: String,
    private val port: UShort,
    private val intention: Int,
) : Packet {
    constructor(buf: PacketBuffer) : this(buf.readVarInt(), buf.readString(255), buf.readUShort(), buf.readVarInt())

    override fun write(buf: PacketBuffer) {
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