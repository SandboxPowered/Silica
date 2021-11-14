package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CWorldBorder : PacketPlay {
    override fun read(buf: PacketBuffer) {}
    override fun write(buf: PacketBuffer) {
        buf.writeDouble(0.0)
        buf.writeDouble(0.0)
        buf.writeDouble(1000.0)
        buf.writeDouble(1000.0)
        buf.writeVarLong(1)
        buf.writeVarInt(29999984)
        buf.writeVarInt(2)
        buf.writeVarInt(3)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}