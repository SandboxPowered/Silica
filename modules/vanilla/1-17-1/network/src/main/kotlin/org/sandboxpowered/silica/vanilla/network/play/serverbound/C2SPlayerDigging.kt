package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.api.world.World
import org.sandboxpowered.silica.api.world.state.block.BlockState
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.StateMappingManager
import org.sandboxpowered.silica.vanilla.network.play.clientbound.S2CAcknowledgePlayerDigging

data class C2SPlayerDigging(private val status: Int, private val location: Position, private val face: Byte) :
    PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readVarInt(), buf.readPosition(), buf.readByte())

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(status)
        buf.writePosition(location)
        buf.writeByte(face)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        when (status) {
            2 -> context.mutatePlayer {
                it.breaking = PlayerDigging(this) { success, newState ->
                    context.world.tell(World.Command.DelayedCommand.Perform {
                        packetHandler.sendPacket(
                            S2CAcknowledgePlayerDigging(
                                location,
                                StateMappingManager.INSTANCE[newState],
                                status,
                                success
                            )
                        )
                    })
                }
            }
        }
    }

    data class PlayerDigging(private val packet: C2SPlayerDigging, val acknowldge: (Boolean, BlockState) -> Unit) {
        val location = packet.location
        val status = packet.status
        val face = packet.face
    }
}