package org.sandboxpowered.silica.vanilla.network.play.clientbound

import org.sandboxpowered.silica.api.util.Identifier
import org.sandboxpowered.silica.util.Hardcoding
import org.sandboxpowered.silica.vanilla.network.PacketByteBuf
import org.sandboxpowered.silica.vanilla.network.PacketHandler
import org.sandboxpowered.silica.vanilla.network.PacketPlay
import org.sandboxpowered.silica.vanilla.network.PlayContext

class S2CDeclareTags : PacketPlay {
    override fun read(buf: PacketByteBuf) {}
    override fun write(buf: PacketByteBuf) {
        buf.writeVarInt(5)
        writeEmpty(buf, "block", Hardcoding.BLOCK_TAGS)
        writeEmpty(buf, "item", Hardcoding.ITEM_TAGS)
        writeEmpty(buf, "fluid", Hardcoding.FLUID_TAGS)
        writeEmpty(buf, "entity_type", Hardcoding.ENTITY_TAGS)
        writeEmpty(buf, "game_event", Hardcoding.GAME_EVENT_TAGS)
    }

    fun writeEmpty(buf: PacketByteBuf, type: String, arr: Array<Identifier>) {
        buf.writeIdentity(Identifier(type))
        buf.writeVarInt(arr.size)
        for (identity in arr) {
            buf.writeIdentity(identity)
            buf.writeVarInt(0)
        }
    }

    override fun handle(packetHandler: PacketHandler, context: PlayContext) {}
}