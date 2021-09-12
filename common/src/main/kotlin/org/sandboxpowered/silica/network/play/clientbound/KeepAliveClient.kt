package org.sandboxpowered.silica.network.play.clientbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class KeepAliveClient(private var id: Long = 0) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        id = buf.readLong()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeLong(id)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}