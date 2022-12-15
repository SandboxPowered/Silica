package org.sandboxpowered.silica.api.network

interface NetworkContext {
    fun reply(packet: Packet)
}