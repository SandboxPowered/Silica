package org.sandboxpowered.silica.network.play.serverbound

import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class EntityAction(var entity: Int = -1, var action: Int = -1, var jumpBoost: Int = -1) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        entity = buf.readVarInt()
        action = buf.readVarInt()
        jumpBoost = buf.readVarInt()
    }

    override fun write(buf: PacketByteBuf) {
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