package org.sandboxpowered.silica.vanilla.network.packets.play.clientbound

import org.sandboxpowered.silica.api.network.PacketBuffer
import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PlayContext
import org.sandboxpowered.silica.vanilla.network.packets.PacketPlay
import org.sandboxpowered.silica.vanilla.network.util.Hardcoding

class S2CDeclareTags : PacketPlay {
    override fun read(buf: PacketBuffer) {}
    override fun write(buf: PacketBuffer) {
        buf.writeVarInt(5)
        writeEmpty(buf, "block", Hardcoding.BLOCK_TAGS)
        writeEmpty(buf, "item", Hardcoding.ITEM_TAGS)
        writeEmpty(buf, "fluid", Hardcoding.FLUID_TAGS)
        writeEmpty(buf, "entity_type", Hardcoding.ENTITY_TAGS)
        writeEmpty(buf, "game_event", Hardcoding.GAME_EVENT_TAGS)
    }

    fun writeEmpty(buf: PacketBuffer, type: String, arr: Array<Identifier>) {
        buf.writeIdentifier(Identifier(type))
        buf.writeVarInt(arr.size)
        for (identity in arr) {
            buf.writeIdentifier(identity)
            buf.writeVarInt(0)
        }
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}