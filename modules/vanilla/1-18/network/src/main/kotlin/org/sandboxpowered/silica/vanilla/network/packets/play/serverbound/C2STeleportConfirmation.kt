package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class C2STeleportConfirmation(private val tpId: Int) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readVarInt())

    override fun write(buf: PacketBuffer) {}
    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}