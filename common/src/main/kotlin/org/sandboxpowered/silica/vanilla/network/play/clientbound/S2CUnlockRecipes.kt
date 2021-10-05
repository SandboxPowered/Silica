package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CUnlockRecipes : PacketPlay {
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