package org.sandboxpowered.silica.vanilla.network

interface HandledPacket : PacketBase {
    fun handle(packetHandler: PacketHandler, connection: Connection)
}