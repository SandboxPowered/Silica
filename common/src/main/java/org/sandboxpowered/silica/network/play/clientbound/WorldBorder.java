package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.PacketByteBuf;
import org.sandboxpowered.silica.network.PacketHandler;
import org.sandboxpowered.silica.network.PacketPlay;
import org.sandboxpowered.silica.network.PlayContext;

public class WorldBorder implements PacketPlay {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeDouble(0);
        buf.writeDouble(0);
        buf.writeDouble(1000);
        buf.writeDouble(1000);
        buf.writeVarLong(1);
        buf.writeVarInt(29999984);
        buf.writeVarInt(2);
        buf.writeVarInt(3);
    }

    @Override
    public void handle(PacketHandler packetHandler, PlayContext context) {

    }
}
