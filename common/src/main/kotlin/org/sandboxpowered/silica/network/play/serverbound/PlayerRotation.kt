package org.sandboxpowered.silica.network.play.serverbound

import org.sandboxpowered.silica.component.VanillaPlayerInput
import org.sandboxpowered.silica.network.PacketByteBuf
import org.sandboxpowered.silica.network.PacketHandler
import org.sandboxpowered.silica.network.PacketPlay
import org.sandboxpowered.silica.network.PlayContext

class PlayerRotation(
    private var yaw: Float = 0f,
    private var pitch: Float = 0f,
    private var onGround: Boolean = false,
) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        yaw = buf.readFloat()
        pitch = buf.readFloat()
        onGround = buf.readBoolean()
    }

    override fun write(buf: PacketByteBuf) {
        buf.writeFloat(yaw)
        buf.writeFloat(pitch)
        buf.writeBoolean(onGround)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
        context.mutatePlayer {
            it.wantedYaw = yaw
            it.wantedPitch = pitch
        }
    }
}