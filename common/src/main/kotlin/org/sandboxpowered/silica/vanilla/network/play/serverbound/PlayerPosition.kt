package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class PlayerPosition(
    private var x: Double = 0.0, private var y: Double = 0.0, private var z: Double = 0.0,
    private var onGround: Boolean = false,
) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        x = buf.readDouble()
        y = buf.readDouble()
        z = buf.readDouble()
        onGround = buf.readBoolean()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeBoolean(onGround)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        context.mutatePlayer { it.wantedPosition[x, y] = z }
    }
}