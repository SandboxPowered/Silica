package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class ClientSettings(
    private var language: String,
    private var renderDistance: Byte,
    private var chatMode: Int,
    private var enableColour: Boolean,
    private var displayedSkin: UByte,
    private var hand: Int,
    private var textFiltering: Boolean,
) : PacketPlay {
    constructor(buf: PacketByteBuf) : this(
        buf.readString(16),
        buf.readByte(),
        buf.readVarInt(),
        buf.readBoolean(),
        buf.readUByte(),
        buf.readVarInt(),
        buf.readBoolean()
    )

    override fun write(buf: PacketByteBuf) {}
    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}