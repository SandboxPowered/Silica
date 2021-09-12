package org.sandboxpowered.silica.network.play.serverbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class HandSwingAnimation(var hand: Int = -1) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        hand = buf.readVarInt()
    }

    override fun write(buf: PacketByteBuf) {
        TODO("Not yet implemented")
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        // TODO
    }
}