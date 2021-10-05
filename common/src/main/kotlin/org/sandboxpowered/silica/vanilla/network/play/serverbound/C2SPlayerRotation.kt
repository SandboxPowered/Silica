package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SPlayerRotation(
    private val yaw: Float,
    private val pitch: Float,
    private val onGround: Boolean,
) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(buf.readFloat(), buf.readFloat(), buf.readBoolean())

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