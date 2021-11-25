package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class S2CHeldItemChange(private val slot: Byte) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readByte())

    override fun write(buf: PacketBuffer) {
        buf.writeByte(slot)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
    }
}