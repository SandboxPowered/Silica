package org.sandboxpowered.silica.vanilla.network.packets.play.serverbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay

class C2SClientSettings(
    private val language: String,
    private val renderDistance: Byte,
    private val chatMode: Int,
    private val enableColour: Boolean,
    private val displayedSkin: UByte,
    private val hand: Int,
    private val textFiltering: Boolean,
) : PacketPlay {
    constructor(buf: PacketBuffer) : this(
        buf.readString(16),
        buf.readByte(),
        buf.readVarInt(),
        buf.readBoolean(),
        buf.readUByte(),
        buf.readVarInt(),
        buf.readBoolean()
    )

    override fun write(buf: PacketBuffer) {}
    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}