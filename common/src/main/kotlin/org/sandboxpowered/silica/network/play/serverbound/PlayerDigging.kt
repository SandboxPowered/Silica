package org.sandboxpowered.silica.network.play.serverbound

import org.sandboxpowered.silica.block.Blocks
import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext
import org.sandboxpowered.silica.network.play.clientbound.AcknowledgePlayerDigging
import org.sandboxpowered.silica.util.math.Position

class PlayerDigging(private var status: Int, private var location: Position?, private var face: Byte) : PacketPlay {

    constructor() : this(-1, null, -1)

    override fun read(buf: PacketByteBuf) {
        status = buf.readVarInt()
        location = buf.readPosition()
        face = buf.readByte()
    }

    override fun write(buf: PacketByteBuf) {

    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        println("Player Digging: $status $location $face")
        location?.let { pos ->
            context.mutateWorld {
                it.setBlockState(pos, Blocks.AIR.get().defaultState)
                packetHandler.sendPacket(
                    AcknowledgePlayerDigging(
                        pos,
                        context.server.stateManager.toVanillaId(it.getBlockState(pos)),
                        status,
                        true
                    )
                )
            }
        }
    }
}