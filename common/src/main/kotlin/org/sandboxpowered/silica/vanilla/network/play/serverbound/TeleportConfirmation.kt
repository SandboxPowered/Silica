package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class TeleportConfirmation(private var tpId: Int = 0) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        tpId = buf.readVarInt()
    }

    override fun write(buf: PacketByteBuf) {}
    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}