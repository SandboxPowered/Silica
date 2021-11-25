package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class C2SPlayerAbilities(private val flags: Byte) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readByte())

    override fun write(buf: PacketBuffer) {
        buf.writeByte(flags)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        val flight = (flags.toInt() and 0x02) != 0

        context.mutatePlayer {
            it.flying = flight
        }
    }
}