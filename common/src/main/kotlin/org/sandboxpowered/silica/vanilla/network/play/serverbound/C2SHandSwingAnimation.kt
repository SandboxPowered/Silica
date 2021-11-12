package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.getLogger
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SHandSwingAnimation(val hand: Int) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readVarInt())

    override fun write(buf: PacketBuffer) {
        TODO("Not yet implemented")
    }

    private val logger = getLogger()

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        logger.info(this)
        // TODO
    }
}