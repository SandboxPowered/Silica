package org.sandboxpowered.silica.network

interface Packet : PacketBase {
    fun handle(packetHandler: PacketHandler, connection: Connection)
}