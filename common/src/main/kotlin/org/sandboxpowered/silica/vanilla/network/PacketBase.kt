package org.sandboxpowered.silica.vanilla.network

interface PacketBase {
    @Deprecated("use PacketByteBuf constructor instead")
    fun read(buf: PacketByteBuf) {
    }

    fun write(buf: PacketByteBuf)
}