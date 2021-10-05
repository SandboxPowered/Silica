package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SKeepAliveServer(private var id: Long) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readLong())

    override fun write(buf: PacketByteBuf) {
        buf.writeLong(id)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        packetHandler.connection.calculatePing(id)
    }
}