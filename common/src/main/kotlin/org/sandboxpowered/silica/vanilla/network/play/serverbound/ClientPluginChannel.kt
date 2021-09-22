package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.util.Identifier

class ClientPluginChannel(
    private var channel: Identifier? = null, private var data: ByteArray = byteArrayOf(),
) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        channel = buf.readIdentity()
        data = buf.readByteArray()
    }

    override fun write(buf: PacketByteBuf) {}
    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}