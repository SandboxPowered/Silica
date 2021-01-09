package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.Connection;
import org.sandboxpowered.silica.network.Packet;
import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;

public class WorldBorder implements Packet {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(3);
        buf.writeVarInt(0);
        buf.writeVarInt(0);
        buf.writeDouble(100);
        buf.writeDouble(100);
        buf.writeVarLong(1);
        buf.writeVarInt(1);
        buf.writeVarInt(2);
        buf.writeVarInt(3);
    }

    @Override
    public void handle(PacketHandler packetHandler, Connection connection) {

    }
}
