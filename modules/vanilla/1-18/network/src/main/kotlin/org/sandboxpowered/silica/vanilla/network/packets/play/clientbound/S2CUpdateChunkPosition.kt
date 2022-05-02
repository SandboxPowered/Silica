package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class S2CUpdateChunkPosition(
    private val x: Int,
    private val y: Int
) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readVarInt(), buf.readVarInt())

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(x)
        buf.writeVarInt(y)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}