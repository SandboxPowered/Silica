package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class C2SEntityAction(val entity: Int, val action: Int, val jumpBoost: Int) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readVarInt(), buf.readVarInt(), buf.readVarInt())

    override fun write(buf: PacketBuffer) {
        TODO("Not yet implemented")
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        if (action in 0..1 || action in 3..4) {
            context.mutatePlayer {
                if (action < 2) it.sneaking = action == 0
                else it.jumping = action == 3
            }
        }
        // TODO Not yet implemented
    }
}