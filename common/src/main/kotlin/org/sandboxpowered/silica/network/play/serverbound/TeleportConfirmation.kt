package org.sandboxpowered.silica.network.play.serverbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class TeleportConfirmation(private var tpId: Int = 0) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        tpId = buf.readVarInt()
    }

    override fun write(buf: PacketByteBuf) {}
    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}