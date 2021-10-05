package org.sandboxpowered.silica.vanilla.network.handshake.clientbound

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.handshake.serverbound.S2CStatusResponse

class C2SStatusRequest() : Packet {
    constructor(buf: PacketByteBuf) : this()

    override fun write(buf: PacketByteBuf) {}

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        packetHandler.sendPacket(S2CStatusResponse(connection.motd))
    }
}