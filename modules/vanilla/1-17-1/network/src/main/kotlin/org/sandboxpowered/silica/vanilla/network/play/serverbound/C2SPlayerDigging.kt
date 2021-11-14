package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.math.Position
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SPlayerDigging(private val status: Int, private val location: Position, private val face: Byte) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readVarInt(), buf.readPosition(), buf.readByte())

    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(status)
        buf.writePosition(location)
        buf.writeByte(face)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
//        println("Player Digging: $status $location $face")
        when (status) {
            0 -> context.mutatePlayer { it.breaking = location }
        }
    }
}