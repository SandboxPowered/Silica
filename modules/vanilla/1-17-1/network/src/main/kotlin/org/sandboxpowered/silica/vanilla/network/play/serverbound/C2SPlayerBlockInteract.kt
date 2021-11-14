package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.joml.Vector3f
import org.sandboxpowered.silica.api.entity.InteractionContext
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.Hand
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SPlayerBlockInteract(
    val hand: Int,
    val location: Position,
    val face: Int,
    val cursorX: Float,
    val cursorY: Float,
    val cursorZ: Float,
    val insideBlock: Boolean,
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(
        buf.readVarInt(),
        buf.readPosition(),
        buf.readVarInt(),
        buf.readFloat(),
        buf.readFloat(),
        buf.readFloat(),
        buf.readBoolean()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(hand)
        buf.writePosition(location)
        buf.writeVarInt(face)
        buf.writeVector3f(cursorX, cursorY, cursorZ)
        buf.writeBoolean(insideBlock)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        // TODO
        context.mutatePlayer {
            it.interacting = InteractionContext(
                Hand.MAIN_HAND,
                location,
                Direction.byId(face),
                Vector3f(cursorX, cursorY, cursorZ),
                insideBlock
            )
        }
    }
}