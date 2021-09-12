package org.sandboxpowered.silica.network.play.clientbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class UpdateChunkPosition(private var x: Int = 0, private var y: Int = 0) : PacketPlay {

    override fun read(buf: PacketByteBuf) {}
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(x)
        buf.writeVarInt(y)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}