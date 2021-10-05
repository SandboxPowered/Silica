package org.sandboxpowered.silica.vanilla.network.login.clientbound

import net.kyori.adventure.text.Component
import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class S2CDisconnect(private val reason: Component) : Packet {
    constructor(buf: PacketByteBuf) : this(buf.readText())

    override fun write(buf: PacketByteBuf) {
        buf.writeText(reason)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}