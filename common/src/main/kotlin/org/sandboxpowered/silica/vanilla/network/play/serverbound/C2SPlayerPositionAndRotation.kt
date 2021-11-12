package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SPlayerPositionAndRotation(
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val yaw: Float,
    private val pitch: Float,
    private val onGround: Boolean,
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(
        buf.readDouble(),
        buf.readDouble(),
        buf.readDouble(),
        buf.readFloat(),
        buf.readFloat(),
        buf.readBoolean()
    )

    override fun write(buf: PacketBuffer) {
        buf.writeDouble(x)
        buf.writeDouble(y)
        buf.writeDouble(z)
        buf.writeFloat(yaw)
        buf.writeFloat(pitch)
        buf.writeBoolean(onGround)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        context.mutatePlayer {
            it.wantedPosition[x, y] = z
            it.wantedYaw = yaw
            it.wantedPitch = pitch
        }
    }
}