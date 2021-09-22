package org.sandboxpowered.silica.vanilla.network

interface Packet : PacketBase {
    fun handle(packetHandler: PacketHandler, connection: Connection)
}