package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SGenerateStructure(
    private val position: Position,
    private val levels: Int,
    private val keepJigsaws: Boolean
) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readPosition(), buf.readVarInt(), buf.readBoolean())

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(levels)
        buf.writePosition(position)
        buf.writeBoolean(keepJigsaws)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle generate structure
    }
}