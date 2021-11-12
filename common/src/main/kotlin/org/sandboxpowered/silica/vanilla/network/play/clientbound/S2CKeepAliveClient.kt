package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CKeepAliveClient(private val id: Long) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readLong())

    override fun write(buf: PacketBuffer) {
        buf.writeLong(id)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}