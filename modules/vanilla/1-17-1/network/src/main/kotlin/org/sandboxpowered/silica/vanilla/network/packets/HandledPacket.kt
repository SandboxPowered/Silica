package org.sandboxpowered.silica.vanilla.network.packets

import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.PacketHandler

interface HandledPacket : PacketBase {
    fun handle(packetHandler: PacketHandler, connection: Connection)
}