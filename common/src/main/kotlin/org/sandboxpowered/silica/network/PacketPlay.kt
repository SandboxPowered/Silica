package org.sandboxpowered.silica.network

sealed interface PacketPlay : PacketBase {
    fun handle(packetHandler: PacketHandler, context: PlayContext)
}