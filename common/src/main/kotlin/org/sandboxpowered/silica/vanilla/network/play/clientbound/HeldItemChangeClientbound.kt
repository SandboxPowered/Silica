package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class HeldItemChangeClientbound(private var slot: Byte) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readByte())

    override fun write(buf: PacketByteBuf) {
        buf.writeByte(slot)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
    }
}