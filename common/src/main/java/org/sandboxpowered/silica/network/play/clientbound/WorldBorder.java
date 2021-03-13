package org.sandboxpowered.silica.network.play.clientbound;

import org.sandboxpowered.silica.network.*;

public class WorldBorder implements PacketPlay {
    @Override
    public void read(PacketByteBuf buf) {

    }

    @Override
    public void write(PacketByteBuf buf) {
        buf.writeVarInt(3);
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
