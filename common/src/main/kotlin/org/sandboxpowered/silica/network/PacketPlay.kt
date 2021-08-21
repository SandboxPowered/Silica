package org.sandboxpowered.silica.network

interface PacketPlay : PacketBase {
    fun handle(packetHandler: PacketHandler, context: PlayContext)
}