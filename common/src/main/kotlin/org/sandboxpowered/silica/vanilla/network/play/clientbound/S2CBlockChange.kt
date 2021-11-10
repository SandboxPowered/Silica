package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CBlockChange(val location: Position, val blockId: Int) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readPosition(), buf.readVarInt())

    override fun write(buf: PacketByteBuf) {
        buf.writePosition(location)
        buf.writeVarInt(blockId)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        TODO("Not yet implemented")
    }
}