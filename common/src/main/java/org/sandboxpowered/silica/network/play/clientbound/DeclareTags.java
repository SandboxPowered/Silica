package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayConnection;
import org.sandboxpowered.silica.util.Hardcoding;

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

    public void writeEmpty(PacketByteBuf buf, Identity[] arr) {
        buf.writeVarInt(arr.length);
        for (Identity identity : arr) {
            buf.writeIdentity(identity);
            buf.writeVarInt(0);
        }
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayConnection connection) {

    }
}