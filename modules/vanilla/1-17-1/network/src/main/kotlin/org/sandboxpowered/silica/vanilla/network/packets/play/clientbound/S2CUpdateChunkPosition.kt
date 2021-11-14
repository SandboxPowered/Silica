package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CUpdateChunkPosition(private var x: Int = 0, private var y: Int = 0) : PacketPlay {

    override fun read(buf: PacketBuffer) {}
    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(x)
        buf.writeVarInt(y)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}