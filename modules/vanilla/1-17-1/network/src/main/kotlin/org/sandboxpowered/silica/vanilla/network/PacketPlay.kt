package org.sandboxpowered.silica.vanilla.network

interface PacketPlay : PacketBase {
    fun handle(packetHandler: PacketHandler, context: PlayContext)
}