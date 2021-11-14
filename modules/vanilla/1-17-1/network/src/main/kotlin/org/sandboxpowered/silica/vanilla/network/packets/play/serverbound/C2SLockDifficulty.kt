package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SLockDifficulty(private val locked: Boolean) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readBoolean())

    override fun write(buf: PacketBuffer) {
        buf.writeBoolean(locked)
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO: handle set difficulty
    }
}