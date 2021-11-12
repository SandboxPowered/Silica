package org.sandboxpowered.silica.vanilla.network

import org.sandboxpowered.silica.api.network.PacketBuffer

interface PacketBase {
    @Deprecated("use PacketByteBuf constructor instead")
    fun read(buf: PacketBuffer) {
    }

    fun write(buf: PacketBuffer)
}