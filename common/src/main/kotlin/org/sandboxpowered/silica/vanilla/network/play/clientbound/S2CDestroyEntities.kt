package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CDestroyEntities(val entities: IntArray) : PacketPlay {

    constructor(buf: PacketByteBuf) : this(buf.readVarIntArray())

    override fun write(buf: PacketByteBuf) {
        buf.writeVarIntArray(entities)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}