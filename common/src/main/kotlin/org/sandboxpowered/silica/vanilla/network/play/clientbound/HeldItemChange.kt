package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class HeldItemChange(private var slot: Byte = 0) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        slot = buf.readByte()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeByte(slot.toInt())
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}