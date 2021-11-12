package org.sandboxpowered.silica.api.network

interface Packet {
    fun write(writer: PacketBuffer)
}