package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class S2CBlockChange(val location: Position, val blockId: Int) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readPosition(), buf.readVarInt())

    override fun write(buf: PacketBuffer) {
        buf.writePosition(location)
        buf.writeVarInt(blockId)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}