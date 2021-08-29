package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayContext;
import org.sandboxpowered.silica.util.Hardcoding;
import org.sandboxpowered.silica.util.Identifier;

public class DeclareTags implements PacketPlay {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(5);
        writeEmpty(buf, "block", Hardcoding.BLOCK_TAGS);
        writeEmpty(buf, "item", Hardcoding.ITEM_TAGS);
        writeEmpty(buf, "fluid", Hardcoding.FLUID_TAGS);
        writeEmpty(buf, "entity_type", Hardcoding.ENTITY_TAGS);
        writeEmpty(buf, "game_event", Hardcoding.GAME_EVENT_TAGS);
    }

    public void writeEmpty(PacketByteBuf buf, String type, Identifier[] arr) {
        buf.writeIdentity(Identifier.Companion.of(type));
        buf.writeVarInt(arr.length);
        for (Identifier identity : arr) {
            buf.writeIdentity(identity);
            buf.writeVarInt(0);
        }
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}