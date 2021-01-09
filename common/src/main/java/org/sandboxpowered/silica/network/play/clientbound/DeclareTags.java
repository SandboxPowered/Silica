package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.api.util.Identity;
import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.util.Hardcoding;

public class DeclareTags implements Packet {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        writeEmpty(buf, Hardcoding.BLOCKTAGS);
        writeEmpty(buf, Hardcoding.ITEMTAGS);
        writeEmpty(buf, Hardcoding.FLUIDTAGS);
        writeEmpty(buf, Hardcoding.ENTITYTAGS);
    }

    public void writeEmpty(PacketByteBuf buf, Identity[] arr) {
        buf.writeVarInt(arr.length);
        for (Identity identity : arr) {
            buf.writeIdentity(identity);
            buf.writeVarInt(0);
        }
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}