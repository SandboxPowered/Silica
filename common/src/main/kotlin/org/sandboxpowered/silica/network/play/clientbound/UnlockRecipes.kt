package org.sandboxpowered.silica.network.play.clientbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class UnlockRecipes : PacketPlay {
    override fun read(buf: PacketByteBuf) {}
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(0)
        buf.writeBoolean(false)
        buf.writeBoolean(false)
        buf.writeBoolean(false)
        buf.writeBoolean(false)
        buf.writeBoolean(false)
        buf.writeBoolean(false)
        buf.writeBoolean(false)
        buf.writeBoolean(false)
        buf.writeVarInt(0)
        buf.writeVarInt(0)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}