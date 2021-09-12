package org.sandboxpowered.silica.network

sealed interface PacketBase {
    fun read(buf: PacketByteBuf)
    fun write(buf: PacketByteBuf)
}