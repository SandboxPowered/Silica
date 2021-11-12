package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.Direction
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SPlayerBlockPlacement(
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
        TODO("Not yet implemented")
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        // TODO
        context.mutatePlayer {
            it.placing = location.shift(Direction.byId(face))
        }
    }
}