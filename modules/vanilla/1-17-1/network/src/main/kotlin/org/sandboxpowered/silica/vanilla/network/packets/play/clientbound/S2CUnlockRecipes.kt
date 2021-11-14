package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CUnlockRecipes : PacketPlay {
    override fun read(buf: PacketBuffer) {}
    override fun write(buf: PacketBuffer) {
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