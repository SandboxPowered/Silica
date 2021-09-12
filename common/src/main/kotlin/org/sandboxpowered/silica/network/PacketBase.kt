package org.sandboxpowered.silica.network

interface PacketBase {
    fun read(buf: PacketByteBuf)
    fun write(buf: PacketByteBuf)
}