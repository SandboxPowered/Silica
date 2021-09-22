package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class KeepAliveClient(private var id: Long = 0) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        id = buf.readLong()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeLong(id)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}