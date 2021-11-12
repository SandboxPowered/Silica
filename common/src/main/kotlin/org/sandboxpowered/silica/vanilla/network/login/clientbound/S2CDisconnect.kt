package org.sandboxpowered.silica.vanilla.network.login.clientbound

import net.kyori.adventure.text.Component
import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.Connection
import org.sandboxpowered.silica.vanilla.network.Packet
import org.sandboxpowered.silica.vanilla.network.PacketHandler

class S2CDisconnect(private val reason: Component) : Packet {
    constructor(buf: PacketBuffer) : this(buf.readText())

    override fun write(buf: PacketBuffer) {
        buf.writeText(reason)
    }

    override fun handle(packetHandler: PacketHandler, connection: Connection) {}
}