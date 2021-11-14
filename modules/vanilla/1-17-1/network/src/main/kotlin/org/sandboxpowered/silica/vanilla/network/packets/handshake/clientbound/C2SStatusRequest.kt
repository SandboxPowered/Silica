package org.sandboxpowered.silica.vanilla.network.packets.handshake.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.packets.HandledPacket
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.handshake.serverbound.S2CStatusResponse

class C2SStatusRequest() : HandledPacket {
    constructor(buf: PacketBuffer) : this()

    override fun write(buf: PacketBuffer) {}

    override fun handle(packetHandler: PacketHandler, connection: Connection) {
        connection.getMotd {
            packetHandler.sendPacket(S2CStatusResponse(it))
        }
    }
}