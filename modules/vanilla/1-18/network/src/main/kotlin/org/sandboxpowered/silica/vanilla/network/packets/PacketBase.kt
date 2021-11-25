package org.sandboxpowered.silica.vanilla.network.packets

import org.sandboxpowered.silica.api.network.Packet
import org.sandboxpowered.silica.api.network.PacketBuffer

interface PacketBase : Packet {
    @Deprecated("use PacketByteBuf constructor instead")
    fun read(buf: PacketBuffer) {
    }

    override fun write(buf: PacketBuffer)
}