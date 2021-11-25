package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import net.kyori.adventure.text.Component
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import java.util.*

data class S2CChatMessage(val message: Component, val position: Byte, val sender: UUID) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readText(), buf.readByte(), buf.readUUID())

    override fun write(buf: PacketBuffer) {
        buf.writeText(message)
        buf.writeByte(position)
        buf.writeUUID(sender)
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {
    }
}