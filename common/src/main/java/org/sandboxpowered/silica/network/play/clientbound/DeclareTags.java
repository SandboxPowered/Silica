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
        writeEmpty(buf, Hardcoding.BLOCK_TAGS);
        writeEmpty(buf, Hardcoding.ITEM_TAGS);
        writeEmpty(buf, Hardcoding.FLUID_TAGS);
        writeEmpty(buf, Hardcoding.ENTITY_TAGS);
    }

    public void writeEmpty(PacketByteBuf buf, Identifier[] arr) {
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