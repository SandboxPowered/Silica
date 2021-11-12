package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CDeclareCommands : PacketPlay {
    override fun read(buf: PacketBuffer) {}
    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(1)
        buf.writeByte(0)
        buf.writeVarInt(0)
        buf.writeVarInt(0)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}