package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.joml.Vector3f
import org.sandboxpowered.silica.api.entity.InteractionContext
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.network.readEnum
import org.sandboxpowered.silica.api.network.writeEnum
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.Hand
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SPlayerBlockInteract(
    val hand: Int,
    val location: Position,
    val face: Direction,
    val cursor: Vector3f,
    val insideBlock: Boolean,
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(
        buf.readVarInt(),
        buf.readPosition(),
        buf.readEnum(),
        buf.readVector3f(),
        buf.readBoolean()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(hand)
        buf.writePosition(location)
        buf.writeEnum(face)
        buf.writeVector3f(cursor)
        buf.writeBoolean(insideBlock)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        // TODO
        context.mutatePlayer {
            it.interacting = InteractionContext(
                Hand.MAIN_HAND,
                location,
                face,
                cursor,
                insideBlock
            )
        }
    }
}