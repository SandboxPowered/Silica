package org.sandboxpowered.silica.network.play.clientbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class DeclareRecipes : PacketPlay {
    override fun read(buf: PacketByteBuf) {}
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(0)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}