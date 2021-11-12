package org.sandboxpowered.silica.vanilla.network.handshake.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.handshake.serverbound.S2CStatusResponse

class C2SStatusRequest() : Packet {
    constructor(buf: PacketBuffer) : this()

    override fun write(buf: PacketBuffer) {}

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        connection.getMotd {
            packetHandler.sendPacket(S2CStatusResponse(it))
        }
    }
}