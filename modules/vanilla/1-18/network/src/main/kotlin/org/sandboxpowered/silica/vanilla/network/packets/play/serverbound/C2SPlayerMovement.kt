package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class C2SPlayerMovement(private var onGround: Boolean) : PacketPlay {

    constructor(buf: PacketBuffer) : this(buf.readBoolean())

    override fun write(buf: PacketBuffer) {
        buf.writeBoolean(onGround)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}