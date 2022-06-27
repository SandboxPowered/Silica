package org.sandboxpowered.silica.vanilla.network.packets

import org.sandboxpowered.silica.api.network.Packet
import org.sandboxpowered.silica.api.network.PacketBuffer

interface PacketBase : Packet {
    override fun write(buf: PacketBuffer)
}