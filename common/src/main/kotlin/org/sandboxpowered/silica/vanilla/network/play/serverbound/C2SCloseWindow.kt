package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SCloseWindow(private val windowId: UByte) : PacketPlay {
    constructor(buf: PacketBuffer) : this(buf.readUByte())

    override fun write(buf: PacketBuffer) {
        buf.writeByte(windowId.toByte())
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {

    }
}