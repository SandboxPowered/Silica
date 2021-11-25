package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class S2CKeepAliveClient(private val id: Long) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readLong())

    override fun write(buf: PacketBuffer) {
        buf.writeLong(id)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}