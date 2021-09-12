package org.sandboxpowered.silica.network.play.serverbound

import org.sandboxpowered.silica.component.VanillaPlayerInput
import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class PlayerPositionAndRotation(
    private var x: Double = 0.0, private var y: Double = 0.0, private var z: Double = 0.0,
    private var yaw: Float = 0f, private var pitch: Float = 0f,
    private var onGround: Boolean = false,
) : PacketPlay {

    override fun read(buf: PacketByteBuf) {
        x = buf.readDouble()
        y = buf.readDouble()
        z = buf.readDouble()
        yaw = buf.readFloat()
        pitch = buf.readFloat()
        onGround = buf.readBoolean()
    }

    override fun write(buf: PacketByteBuf) {
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