package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class UnlockRecipes implements Packet {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(0);
        buf.writeBoolean(false);
        buf.writeBoolean(false);

        buf.writeBoolean(false);
        buf.writeBoolean(false);

        buf.writeBoolean(false);
        buf.writeBoolean(false);

        buf.writeBoolean(false);
        buf.writeBoolean(false);

        buf.writeVarInt(0);
        buf.writeVarInt(0);

    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
