package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CUpdateChunkPosition(private var x: Int = 0, private var y: Int = 0) : PacketPlay {

    override fun read(buf: PacketByteBuf) {}
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(x)
        buf.writeVarInt(y)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}