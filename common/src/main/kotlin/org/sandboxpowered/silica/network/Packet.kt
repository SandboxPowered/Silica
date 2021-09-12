package org.sandboxpowered.silica.network

sealed interface Packet : PacketBase {
    fun handle(packetHandler: PacketHandler, connection: Connection)
}