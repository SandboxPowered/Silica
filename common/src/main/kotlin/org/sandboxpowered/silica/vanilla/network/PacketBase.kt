package org.sandboxpowered.silica.vanilla.network

interface PacketBase {
    @Deprecated("use PacketByteBuf constructor instead", ReplaceWith("Unit"))
    fun read(buf: PacketByteBuf) = Unit
    fun write(buf: PacketByteBuf)
}