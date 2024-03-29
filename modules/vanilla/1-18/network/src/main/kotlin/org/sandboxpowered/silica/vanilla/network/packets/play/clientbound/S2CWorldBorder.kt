package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class S2CWorldBorder() : PacketPlay {
    constructor(buf: PacketBuffer) : this()

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