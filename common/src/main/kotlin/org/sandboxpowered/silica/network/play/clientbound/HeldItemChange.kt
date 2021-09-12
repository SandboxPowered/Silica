package org.sandboxpowered.silica.network.play.clientbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class HeldItemChange(private var slot: Byte = 0) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        slot = buf.readByte()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeByte(slot.toInt())
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}