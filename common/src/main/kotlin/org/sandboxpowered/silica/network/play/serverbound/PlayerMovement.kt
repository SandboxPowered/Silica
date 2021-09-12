package org.sandboxpowered.silica.network.play.serverbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class PlayerMovement(private var onGround: Boolean = false) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        onGround = buf.readBoolean()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeBoolean(onGround)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}