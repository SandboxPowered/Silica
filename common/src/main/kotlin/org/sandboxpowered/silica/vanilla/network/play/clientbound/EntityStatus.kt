package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class EntityStatus : PacketPlay {
    override fun read(buf: PacketByteBuf) {}
    override fun write(buf: PacketByteBuf) {
        buf.writeInt(0)
        buf.writeByte(24)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}