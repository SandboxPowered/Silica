package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.content.block.Blocks
import org.sandboxpowered.silica.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CAcknowledgePlayerDigging

class C2SPlayerDigging(private val status: Int, private val location: Position, private val face: Byte) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readVarInt(), buf.readPosition(), buf.readByte())

    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(status)
        buf.writePosition(location)
        buf.writeByte(face)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        println("Player Digging: $status $location $face")
        context.mutateWorld {
            it.setBlockState(location, Blocks.AIR.defaultState)
            packetHandler.sendPacket(
                S2CAcknowledgePlayerDigging(
                    location,
                    context.server.stateRemapper[it.getBlockState(location)],
                    status,
                    true
                )
            )
        }
    }
}