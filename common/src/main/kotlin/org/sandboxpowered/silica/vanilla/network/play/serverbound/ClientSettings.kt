package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class ClientSettings(
    private var language: String? = null, private var renderDistance: Byte = 0,
    private var chatMode: Int = 0, private var enableColour: Boolean = false,
    private var displayedSkin: UByte = 0u, private var hand: Int = 0,
    private var textFiltering: Boolean = false,
) : PacketPlay {
    override fun read(buf: PacketByteBuf) {
        language = buf.readString(16)
        renderDistance = buf.readByte()
        chatMode = buf.readVarInt()
        enableColour = buf.readBoolean()
        displayedSkin = buf.readUByte()
        hand = buf.readVarInt()
        textFiltering = buf.readBoolean()
    }

    override fun write(buf: PacketByteBuf) {}
    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}