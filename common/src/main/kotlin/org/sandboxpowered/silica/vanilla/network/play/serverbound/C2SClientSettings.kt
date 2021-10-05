package org.sandboxpowered.silica.vanilla.network.play.serverbound

import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class C2SClientSettings(
    private val language: String,
    private val renderDistance: Byte,
    private val chatMode: Int,
    private val enableColour: Boolean,
    private val displayedSkin: UByte,
    private val hand: Int,
    private val textFiltering: Boolean,
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