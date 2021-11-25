package org.sandboxpowered.silica.vanilla.network.packets

import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext

interface PacketPlay : PacketBase {
    fun handle(packetHandler: PacketHandler, context: PlayContext)
}