package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SLockDifficulty(private val locked: Boolean) : PacketPlay {

    constructor(buf: PacketByteBuf) : this(buf.readBoolean())

    override fun write(buf: PacketByteBuf) {
        buf.writeBoolean(locked)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle set difficulty
    }
}