package org.sandboxpowered.silica.api.network

interface Packet {
    fun write(buf: PacketBuffer)
}