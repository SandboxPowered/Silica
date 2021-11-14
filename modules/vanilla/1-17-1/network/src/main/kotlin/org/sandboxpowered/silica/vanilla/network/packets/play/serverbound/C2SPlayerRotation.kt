package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

data class C2SPlayerRotation(
    private val yaw: Float,
    private val pitch: Float,
    private val onGround: Boolean,
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readFloat(), buf.readFloat(), buf.readBoolean())

    override fun write(buf: PacketBuffer) {
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