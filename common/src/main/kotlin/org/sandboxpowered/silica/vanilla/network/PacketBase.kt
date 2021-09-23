package org.sandboxpowered.silica.vanilla.network

interface PacketBase {
    fun read(buf: PacketByteBuf) {}
    fun write(buf: PacketByteBuf)
}